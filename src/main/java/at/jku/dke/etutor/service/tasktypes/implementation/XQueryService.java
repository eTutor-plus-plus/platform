package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.xq.XMLDefinitionDTO;
import at.jku.dke.etutor.objects.dispatcher.xq.XQExerciseDTO;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.tasktypes.proxy.XQueryProxyService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service with XQuery-specific methods.
 */
@Service
public class XQueryService implements TaskTypeService, TaskGroupTypeService {
    private final XQueryProxyService xQueryProxyService;
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final ApplicationProperties properties;
    public XQueryService(XQueryProxyService xQueryProxyService,
                         AssignmentSPARQLEndpointService assignmentSPARQLEndpointService,
                         ApplicationProperties properties) {
        this.xQueryProxyService = xQueryProxyService;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.properties = properties;
    }

    /**
     * Creates a new XML task group.
     * If the task group is new, the group is created and the XML is added to the XQueryProxy.
     * The file-url of the XML is set for the passed task-group.
     * The description of the task group is updated with a link to view the XML.
     * The description of the task group is updated with a function to include the XML in XQuery-queries.
     * @param newTaskGroupDTO The task group to create
     * @throws MissingParameterException If the XML is not set
     * @throws DispatcherRequestFailedException If the request to the XQueryProxy failed
     */
    @Override
    public void createTaskGroup(NewTaskGroupDTO newTaskGroupDTO) throws TaskTypeSpecificOperationFailedException {
        createOrUpdateTaskGroup(newTaskGroupDTO, true);
    }


    /**
     * Updates an existing XML task group.
     * The XML is updated in the XQueryProxy.
     * The file-url of the XML is set for the passed task-group.
     * The description of the task group is updated with a link to view the XML.
     * The description of the task group is updated with a function to include the XML in XQuery-queries.
     * @param taskGroupDTO the task group to update
     * @throws TaskTypeSpecificOperationFailedException if an error occurs during the update
     */
    @Override
    public void updateTaskGroup(TaskGroupDTO taskGroupDTO) throws TaskTypeSpecificOperationFailedException {
        createOrUpdateTaskGroup(taskGroupDTO, false);
    }

    /**
     *  Deletes a task group from the XQueryProxy.
     * @param taskGroupDTO The task group to delete
     * @throws DispatcherRequestFailedException does not throw this exception, errors are ignored
     */
    @Override
    public void deleteTaskGroup(TaskGroupDTO taskGroupDTO) throws DispatcherRequestFailedException {
        try{
            String taskGroupName = taskGroupDTO.getName().trim().replace(" ", "_");
            xQueryProxyService.deleteXMLofXQTaskGroup(taskGroupName);
        }catch (DispatcherRequestFailedException ignore){
            // Ignore
        }
    }

    /**
     * Creates a new task.
     * If the task group is of type XQuery, the task is created in the XQueryProxy.
     * The dispatcher-id of the task is set for the passed task.
     * @param newTaskAssignmentDTO The task to create
     * @throws MissingParameterException If the task group id or the solution is not set
     * @throws DispatcherRequestFailedException If the request to the XQueryProxy failed
     * @throws NotAValidTaskGroupException If the task group is not of type XQuery
     */
    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException, NotAValidTaskGroupException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString()))
            return;

        if (StringUtils.isBlank(newTaskAssignmentDTO.getTaskGroupId()) ||
            StringUtils.isBlank(newTaskAssignmentDTO.getxQuerySolution())) {
            throw new MissingParameterException("Either the task group id or the solution is not set");
        }

        if(!taskTypeFitsTaskGroupType(newTaskAssignmentDTO)){
            throw new NotAValidTaskGroupException();
        }

        var optId = this.handleTaskCreation(newTaskAssignmentDTO);
        newTaskAssignmentDTO.setTaskIdForDispatcher(optId.map(String::valueOf)
            .orElseThrow(() -> new DispatcherRequestFailedException("Could not create task. No ID returned.")));
    }

    /**
     * Checks whether the task type fits the task group type.
     * Fetches the task-group set in the task assignment and compares the group type with the task assignment type.
     * @param newTaskAssignmentDTO the task assignment
     * @return a boolean indicating whether the task type fits the task group type
     */
    private boolean taskTypeFitsTaskGroupType(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        // Fetch group to compare the group type with the task assignment type
        var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1));
        return group.filter(g -> g.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString()))
            .isPresent();
    }

    /**
     * Updates a task.
     * If the task group is of type XQuery, the task is updated in the XQueryProxy.
     *
     * @param taskAssignmentDTO The task to update
     * @throws MissingParameterException If the solution is not set
     * @throws DispatcherRequestFailedException If the request to the XQueryProxy failed
     */
    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        if(StringUtils.isBlank(taskAssignmentDTO.getxQuerySolution()))
            throw new MissingParameterException("XQuery Solution is missing. Cannot update task.");

        XQExerciseDTO dto = constructXqExerciseDto(taskAssignmentDTO);
        String body = serializeXqExerciseDto(dto);

        xQueryProxyService.updateXQExercise(Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()), body);
    }

    /**
     * Deletes a task from the XQueryProxy.
     * @param taskAssignmentDTO The task to delete
     * @throws DispatcherRequestFailedException does not throw this exception
     */
    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        try{
            xQueryProxyService.deleteXQExercise(Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()));
        }catch (DispatcherRequestFailedException ignore){
            // Ignore
        }
    }

    // private region
    /**
     * Creates or updates a task group.
     * @param newTaskGroupDTO the task group
     * @param isNew a boolean indicating whether the task group is new
     * @throws TaskTypeSpecificOperationFailedException if an error occurs during the creation or update
     */
    private void createOrUpdateTaskGroup(NewTaskGroupDTO newTaskGroupDTO, boolean isNew) throws TaskTypeSpecificOperationFailedException {
        if (StringUtils.isBlank(newTaskGroupDTO.getxQueryDiagnoseXML()) &&
            StringUtils.isBlank(newTaskGroupDTO.getxQuerySubmissionXML())) {
            throw new MissingParameterException("XML is missing. Cannot create task group.");
        }
        XMLDefinitionDTO xmlDefinitionDTO = constructXmlDefinitionDTO(newTaskGroupDTO);

        String body = serializeXmlDefinitionDto(xmlDefinitionDTO);
        var response = xQueryProxyService.addXMLForXQTaskGroup(newTaskGroupDTO.getName().trim().replace(" ", "_"), body);
        var fileURL = response.getBody();

        if(isNew){
            setFileUrlForTaskGroup(newTaskGroupDTO, fileURL);
        }
        updateTaskGroupDescription(newTaskGroupDTO, fileURL);
    }

    /**
     * Constructs a XMLDefinitionDTO from a NewTaskGroupDTO.
     * @param newTaskGroupDTO the task group
     * @return the XMLDefinitionDTO
     */
    private XMLDefinitionDTO constructXmlDefinitionDTO(NewTaskGroupDTO newTaskGroupDTO) {
        // Initialize DTO
        String diagnoseXML = newTaskGroupDTO.getxQueryDiagnoseXML();
        String submissionXML = newTaskGroupDTO.getxQuerySubmissionXML();
        return new XMLDefinitionDTO(diagnoseXML, submissionXML);
    }

    /**
     * Sets the file-url for a task group if it is not blank.
     * @param newTaskGroupDTO the task group
     * @param fileURL the file-url
     */
    private void setFileUrlForTaskGroup(NewTaskGroupDTO newTaskGroupDTO, String fileURL) {
        if (StringUtils.isNotBlank(fileURL)) {
            newTaskGroupDTO.setFileUrl(fileURL);
        }
    }

    /**
     * Updates the description of a task group.
     * Old link and function are removed from the description.
     * A link to view the diagnose XML is added to the description.
     * A function to include the XML in XQuery-queries is added to the description.
     * @param newTaskGroupDTO the task group
     * @param fileURL the file-url
     */
    private void updateTaskGroupDescription(NewTaskGroupDTO newTaskGroupDTO, String fileURL) {
        // Update group-description with link to view the diagnose XML
        String oldDescription = newTaskGroupDTO.getDescription() != null ? newTaskGroupDTO.getDescription() : "";
        String startOfLinks = "<a href='/XML";
        int indexOfLinks = oldDescription.indexOf(startOfLinks);
        if (indexOfLinks == -1){
            startOfLinks = "<a href=\"/XML";
            indexOfLinks = oldDescription.indexOf(startOfLinks);
        }
        if(indexOfLinks != -1){
            oldDescription = oldDescription.substring(0, indexOfLinks);
        }
        String link = "<br><br> <a href='" + properties.getDispatcher().getXqueryXmlFileUrlPrefix() + fileURL.substring(fileURL.contains("=") ? fileURL.indexOf("=") + 1 : 0) + "' target='_blank'>View XML</a>";
        String newDescription = oldDescription + link;
        newDescription += "<br><br> You can include the XML document for this task group using the following function: <br>";
        newDescription += "<b> let $doc := doc('" + fileURL + "') </b>";
        newTaskGroupDTO.setDescription(newDescription);
    }

    /**
     * Serializes a XMLDefinitionDTO to a JSON string.
     * @param xmlDefinitionDTO the XMLDefinitionDTO
     * @return the JSON string
     * @throws TaskTypeSpecificOperationFailedException if an error occurs during the serialization
     */
    private String serializeXmlDefinitionDto(XMLDefinitionDTO xmlDefinitionDTO) throws TaskTypeSpecificOperationFailedException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(xmlDefinitionDTO);
        } catch (JsonProcessingException e) {
            throw new TaskTypeSpecificOperationFailedException(e.getMessage());
        }
    }

    /**
     * Constructs a XQExerciseDTO from a TaskAssignmentDTO.
     * Serializes the DTO.
     * Sends the DTO to the dispatcher.
     *
     * @param newTaskAssignmentDTO the task assignment
     * @return an optional containing the dispatcher-id for the created task, or an empty optional if the creation failed.
     * @throws TaskTypeSpecificOperationFailedException if an error occurs during the creation
     */
    private Optional<Integer> handleTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        Objects.requireNonNull(newTaskAssignmentDTO.getxQuerySolution());

        XQExerciseDTO dto = constructXqExerciseDto(newTaskAssignmentDTO);

        String body = serializeXqExerciseDto(dto);
        String taskGroupName = newTaskAssignmentDTO
            .getTaskGroupId()
            .substring(newTaskAssignmentDTO.getTaskGroupId()
                .indexOf("#") + 1).trim().replace(" ", "_");
        var response = xQueryProxyService.createXQExercise(taskGroupName, body);

        return response.getBody() != null ? Optional.of(response.getBody()) : Optional.empty();
    }

    /**
     * Serializes a XQExerciseDTO to a JSON string.
     * @param dto the XQExerciseDTO
     * @return the JSON string
     * @throws TaskTypeSpecificOperationFailedException if an error occurs during the serialization
     */
    private String serializeXqExerciseDto(XQExerciseDTO dto) throws TaskTypeSpecificOperationFailedException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new TaskTypeSpecificOperationFailedException(e.getMessage());
        }
    }

    /**
     * Constructs a XQExerciseDTO from a TaskAssignmentDTO.
     * @param newTaskAssignmentDTO the task assignment
     * @return the XQExerciseDTO
     */
    private XQExerciseDTO constructXqExerciseDto(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        XQExerciseDTO exerciseDTO = new XQExerciseDTO();
        List<String> sortings = new ArrayList<>();
        String dtoSorting = newTaskAssignmentDTO.getxQueryXPathSorting();
        if (dtoSorting != null)
            sortings.add(dtoSorting);


        exerciseDTO.setQuery(newTaskAssignmentDTO.getxQuerySolution());
        exerciseDTO.setSortedNodes(sortings);
        return exerciseDTO;
    }


}
