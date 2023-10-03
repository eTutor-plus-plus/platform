package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.xq.XMLDefinitionDTO;
import at.jku.dke.etutor.objects.dispatcher.xq.XQExerciseDTO;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
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
     * Creates a new task group or updates an existing one.
     * If the task group is new, the group is created and the XML is added to the XQueryProxy.
     * If the task group already exists, the XML is updated in the XQueryProxy.
     * The file-url of the XML is updated in the RDF-Graph.
     * The description of the task group is updated with a link to view the XML.
     * The description of the task group is updated with a function to include the XML in XQuery-queries.
     * @param taskGroupDTO The task group to create or update
     * @param isNew True if the task group is new, false if the task group already exists
     * @throws MissingParameterException If the XML is not set
     * @throws DispatcherRequestFailedException If the request to the XQueryProxy failed
     */
    @Override
    public void createOrUpdateTaskGroup(TaskGroupDTO taskGroupDTO, boolean isNew) throws MissingParameterException, DispatcherRequestFailedException {
        if (StringUtils.isBlank(taskGroupDTO.getxQueryDiagnoseXML()) &&
            StringUtils.isBlank(taskGroupDTO.getxQuerySubmissionXML())) {
            throw new MissingParameterException();
        }
        // Initialize DTO
        String diagnoseXML = taskGroupDTO.getxQueryDiagnoseXML();
        String submissionXML = taskGroupDTO.getxQuerySubmissionXML();
        XMLDefinitionDTO body = new XMLDefinitionDTO(diagnoseXML, submissionXML);
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException(e.getMessage());
        }
        var response = xQueryProxyService.addXMLForXQTaskGroup(taskGroupDTO.getName().trim().replace(" ", "_"), jsonBody);
        var fileURL = response.getBody();

        if (fileURL != null && StringUtils.isNotBlank(fileURL)) {
            // Update file-url in RDF-Graph (can be used in XQuery-queries to reference the group/XML)
            assignmentSPARQLEndpointService.addXMLFileURL(taskGroupDTO, fileURL);

            // Update group-description with link to view the diagnose XML
            String oldDescription = taskGroupDTO.getDescription() != null ? taskGroupDTO.getDescription() : "";
            String startOfLinks = "<a href=\"/XML";
            int indexOfLinks = oldDescription.indexOf(startOfLinks);
            if (indexOfLinks != -1) oldDescription = oldDescription.substring(0, indexOfLinks);

            String link = "<br><br> <a href='" + properties.getDispatcher().getXqueryXmlFileUrlPrefix() + fileURL.substring(fileURL.contains("=") ? fileURL.indexOf("=") + 1 : 0) + "' target='_blank'>View XML</a>";
            String newDescription = oldDescription + link;
            newDescription += "<br><br> You can include the XML document for this task group using the following function: <br>";
            newDescription += "<b> let $doc := doc('" + fileURL + "') </b>";
            taskGroupDTO.setDescription(newDescription);
            assignmentSPARQLEndpointService.modifyTaskGroup(taskGroupDTO);
        }
    }

    /**
     *  Deletes a task group from the XQueryProxy.
     * @param taskGroupDTO The task group to delete
     * @throws DispatcherRequestFailedException does not throw this exception
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
     * @param newTaskAssignmentDTO The task to create
     * @throws MissingParameterException If the task group id or the solution is not set
     * @throws DispatcherRequestFailedException If the request to the XQueryProxy failed
     * @throws NotAValidTaskGroupException If the task group is not of type XQuery
     */
    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException, NotAValidTaskGroupException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString()))
            return;

        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId())
            && StringUtils.isNotBlank(newTaskAssignmentDTO.getxQuerySolution())) {

            // Fetch group to compare the group type with the task assignment type
            var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1));
            group.filter(g -> g.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())).orElseThrow(NotAValidTaskGroupException::new);

            var optId = this.handleTaskCreation(newTaskAssignmentDTO);
            optId.map(String::valueOf).ifPresent(newTaskAssignmentDTO::setTaskIdForDispatcher);
        } else{ // Either is the id not set, or group + solution is not set
            throw new MissingParameterException("Either the task group id or the solution is not set");
        }
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
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        if(StringUtils.isBlank(taskAssignmentDTO.getxQuerySolution()))
            throw new MissingParameterException("XQuery Solution is missing. Cannot update task.");

        // Initialize DTO
        XQExerciseDTO body = new XQExerciseDTO();


        List<String> sortings = new ArrayList<>();
        String dtoSorting = taskAssignmentDTO.getxQueryXPathSorting();
        if (dtoSorting != null)
            sortings.add(dtoSorting);
        body.setQuery(taskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException(e.getMessage());
        }
        // Proxy request
        xQueryProxyService.updateXQExercise(Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()), jsonBody);
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

    private Optional<Integer> handleTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        Objects.requireNonNull(newTaskAssignmentDTO.getxQuerySolution());

        // Initialize DTO
        XQExerciseDTO body = new XQExerciseDTO();

        List<String> sortings = new ArrayList<>();
        String dtoSorting = newTaskAssignmentDTO.getxQueryXPathSorting();
        if (dtoSorting != null)
            sortings.add(dtoSorting);


        body.setQuery(newTaskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        // Send request
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        var response = xQueryProxyService.createXQExercise(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1).trim().replace(" ", "_"), jsonBody);

        // Return ID of the task
        return response.getBody() != null ? Optional.of(response.getBody()) : Optional.empty();
    }
}
