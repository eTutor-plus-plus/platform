package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogExerciseDTO;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogTaskGroupDTO;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogTermDescriptionDTO;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.tasktypes.proxy.DatalogProxyService;
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

import java.util.*;

/**
 * Functionality related to the Datalog task type.
 */
@Service
public class DatalogService implements TaskTypeService, TaskGroupTypeService {
    private final DatalogProxyService datalogProxyService;
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final ApplicationProperties properties;

    public DatalogService(DatalogProxyService datalogProxyService,
                          AssignmentSPARQLEndpointService assignmentSPARQLEndpointService,
                          ApplicationProperties properties) {
        this.datalogProxyService = datalogProxyService;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.properties = properties;
    }

    /**
     * Creates or updates a task group in the dispatcher.
     * If the task group is new, it will be created in the dispatcher and the id will be set in the RDF-graph.
     * If the task group already exists, it will be updated in the dispatcher.
     *
     * @param newTaskGroupDTO the task group to create or update
     * @param isNew          true if the task group is new, false if it already exists
     * @throws MissingParameterException not thrown currently
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     */
    @Override
    public void createOrUpdateTaskGroup(TaskGroupDTO newTaskGroupDTO, boolean isNew) throws MissingParameterException, DispatcherRequestFailedException {
        Objects.requireNonNull(newTaskGroupDTO);
        int id = assignmentSPARQLEndpointService.getDispatcherIdForTaskGroup(newTaskGroupDTO);
        if (!isNew) {
            datalogProxyService.updateDLGTaskGroup(id, newTaskGroupDTO.getDatalogFacts()).getStatusCodeValue();
            return;
        }// group is new

        // Initialize DTO
        var group = new DatalogTaskGroupDTO();
        group.setFacts(newTaskGroupDTO.getDatalogFacts());
        group.setName(newTaskGroupDTO.getName());

        // Proxy request
        Integer body = null;
        try {
            var response = datalogProxyService.createDLGTaskGroup(new ObjectMapper().writeValueAsString(group));
            body = response.getBody();
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not parse task group to JSON. " + e.getMessage());
        }
        if(body == null) throw new DispatcherRequestFailedException("Request to dispatcher to create DLG task group failed.");
        id = body;

        // Set received id for task group in RDF-graph
        assignmentSPARQLEndpointService.setDispatcherIdForTaskGroup(newTaskGroupDTO, id);

        // Update description of task group with link to the facts
        String link = "<br> <a href='" + properties.getDispatcher().getDatalogFactsUrlPrefix() + id + "' target='_blank'>Facts</a>";
        newTaskGroupDTO.setDescription(newTaskGroupDTO.getDescription() != null ? newTaskGroupDTO.getDescription() + link : link);

        // Update task group in RDF to include the new description
        assignmentSPARQLEndpointService.modifyTaskGroup(newTaskGroupDTO);
    }

    /**
     * Deletes a task group in the dispatcher.
     * @param taskGroupDTO the task group to delete
     * @throws DispatcherRequestFailedException does not throw this exception currently, as we ignore it if deletion fails
     */
    @Override
    public void deleteTaskGroup(TaskGroupDTO taskGroupDTO) throws DispatcherRequestFailedException {
        try{
            int id = assignmentSPARQLEndpointService.getDispatcherIdForTaskGroup(taskGroupDTO);
            datalogProxyService.deleteDLGTaskGroup(id);
        }catch (DispatcherRequestFailedException ignored){
            // Ignore exception, as we do not want to throw an exception if deletion fails
        }
    }

    /**
     * Creates a task in the dispatcher.
     * @param newTaskAssignmentDTO the task to create
     * @throws MissingParameterException if not enough parameters have been provided to create a task
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     * @throws NotAValidTaskGroupException if the task group is not a valid task group for this task type
     */
    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException, NotAValidTaskGroupException {
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.DatalogTask.toString()))
            return;

        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId())
            && StringUtils.isNotBlank(newTaskAssignmentDTO.getDatalogSolution())
            && StringUtils.isNotBlank(newTaskAssignmentDTO.getDatalogQuery())) {

            // Fetch assigned task-group to check if the group-type matches the task type
            var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1));
            group.filter(g -> g.getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString())).orElseThrow(NotAValidTaskGroupException::new);

            // Create task
            var optId = this.handleTaskCreation(newTaskAssignmentDTO);

            // Set the returned id of the task
            optId.map(String::valueOf)
                .ifPresent(newTaskAssignmentDTO::setTaskIdForDispatcher);
        } else{
            throw new MissingParameterException("Not enough parameters have been provided to create a Datalog task");
        }
    }

    /**
     * Updates a task in the dispatcher.
     * @param taskAssignmentDTO the task to update
     * @throws MissingParameterException if not enough parameters have been provided to update a task
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     */
    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        if(StringUtils.isNotBlank(taskAssignmentDTO.getDatalogSolution()) && StringUtils.isNotBlank(taskAssignmentDTO.getDatalogQuery())){
            // Initialize DTO from task assignment
            var exercise = constructDatalogDtoFromAssignmentDto(taskAssignmentDTO);

            // Proxy request to dispatcher
            datalogProxyService.modifyDLGExercise(exercise, Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()));
        }else{
            throw new MissingParameterException("DatalogSolution or DatalogQuery is missing");
        }

    }

    /**
     * Deletes a task in the dispatcher.
     * @param taskAssignmentDTO the task to delete
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     */
    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            datalogProxyService.deleteDLGExercise(id);
        }catch(NumberFormatException | DispatcherRequestFailedException ignore){
            // ignore exception, as we do not want to throw an exception if deletion fails
        }

    }

    private Optional<Integer> handleTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        var exerciseDTO = constructDatalogDtoFromAssignmentDto(newTaskAssignmentDTO);
        var response = datalogProxyService.createDLGExercise(exerciseDTO);
        if(response.getBody() != null)
            return Optional.of(response.getBody());
        else
            return Optional.empty();
    }

    private DatalogExerciseDTO constructDatalogDtoFromAssignmentDto(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        // Initialize DTO
        DatalogExerciseDTO exerciseDTO = new DatalogExerciseDTO();
        List<String> queries = new ArrayList<>();

        // Split the queries and initialize list of queries
        Arrays.stream(newTaskAssignmentDTO.getDatalogQuery().split(";")).forEach(x -> queries.add(x.trim().replace("\r", "").replace("\n", "").replace(" ", "")));

        exerciseDTO.setQueries(queries);
        exerciseDTO.setSolution(newTaskAssignmentDTO.getDatalogSolution());

        // Parse the String of unchecked terms into a list of DTOs
        if(newTaskAssignmentDTO.getDatalogUncheckedTerms() != null)
            exerciseDTO.setUncheckedTerms(parseUncheckedTerms(newTaskAssignmentDTO.getDatalogUncheckedTerms()));

        // Set the id of the facts by a quick and dirty solution of initializing a new task group with the id of the
        // task group set in the task, which can be used to retrieve the id of the task group
        var tempTaskGroup = new TaskGroupDTO();
        tempTaskGroup.setId(newTaskAssignmentDTO.getTaskGroupId());
        exerciseDTO.setFactsId(assignmentSPARQLEndpointService.getDispatcherIdForTaskGroup(tempTaskGroup));
        return exerciseDTO;
    }

    /**
     * Parses the string of unchecked datalog terms into a list of {@link DatalogTermDescriptionDTO}
     * @param datalogUncheckedTerms the string defining the unchecked terms
     * @return the list
     */
    private List<DatalogTermDescriptionDTO> parseUncheckedTerms(String datalogUncheckedTerms) {
        var termList = Arrays.stream(datalogUncheckedTerms.split("\\.")).toList();
        List<DatalogTermDescriptionDTO> list = new ArrayList<>();
        for(String s : termList){
            if(!s.contains("(") || !s.contains(")")) continue;

            var predicate = s
                .substring(0, s.indexOf("("))
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "");

            var terms = s.substring(s.indexOf("(")+1, s.indexOf(")"));
            var splittedTerms = terms.split(",");

            int i = 1;
            for(String s1 : splittedTerms){
                if(s1.equals("_")) {
                    i++;
                    continue;
                }

                var term = new DatalogTermDescriptionDTO();
                term.setPredicate(predicate);
                term.setPosition(i+"");
                term.setTerm(s1);
                list.add(term);

                i++;
            }
        }
        return list;
    }
}
