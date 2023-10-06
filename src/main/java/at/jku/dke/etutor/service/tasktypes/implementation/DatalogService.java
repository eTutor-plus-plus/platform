package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogExerciseDTO;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogTaskGroupDTO;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogTermDescriptionDTO;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.client.dke.DatalogClient;
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
    private final DatalogClient datalogClient;
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final ApplicationProperties properties;

    public DatalogService(DatalogClient datalogClient,
                          AssignmentSPARQLEndpointService assignmentSPARQLEndpointService,
                          ApplicationProperties properties) {
        this.datalogClient = datalogClient;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.properties = properties;
    }

    /**
     * Creates a task group in the dispatcher.
     * Sets the received dispatcher-id for the group.
     * Updates the description to contain a link to the facts.
     * @param newTaskGroupDTO the task group to create
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     */
    @Override
    public void createTaskGroup(NewTaskGroupDTO newTaskGroupDTO) throws DispatcherRequestFailedException {
        // Initialize DTO
        DatalogTaskGroupDTO datalogTaskGroupDTO = constructDatalogTaskGroupDto(newTaskGroupDTO);
        Integer dispatcherTaskGroupId = proxyTaskGroupCreationRequestToDispatcher(datalogTaskGroupDTO);

        newTaskGroupDTO.setDispatcherId(String.valueOf(dispatcherTaskGroupId));
        setDispatcherLinkInTaskGroupDescription(String.valueOf(dispatcherTaskGroupId), newTaskGroupDTO);
    }

    /**
     * Updates a task group in the dispatcher.
     * @param taskGroupDTO the task group to update
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     */
    @Override
    public void updateTaskGroup(TaskGroupDTO taskGroupDTO) throws DispatcherRequestFailedException {
        Objects.requireNonNull(taskGroupDTO);
        int id = Integer.parseInt(taskGroupDTO.getDispatcherId());
        if (id != -1) {
            datalogClient.updateDLGTaskGroup(String.valueOf(id), taskGroupDTO.getDatalogFacts());
        }
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
            datalogClient.deleteDLGTaskGroup(id);
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

        if (StringUtils.isBlank(newTaskAssignmentDTO.getTaskGroupId()) ||
            StringUtils.isBlank(newTaskAssignmentDTO.getDatalogSolution()) ||
            StringUtils.isBlank(newTaskAssignmentDTO.getDatalogQuery())) {
            throw new MissingParameterException("Not enough parameters have been provided to create a Datalog task. Either task-group-id, datalog-solution or datalog-query is missing.");
        }

        if(!taskTypeFitsTaskGroupType(newTaskAssignmentDTO)){
            throw new NotAValidTaskGroupException();
        }

        // Create task
        var optId = this.handleTaskCreation(newTaskAssignmentDTO);

        // Set the returned id of the task
        newTaskAssignmentDTO.setTaskIdForDispatcher(optId.map(String::valueOf)
            .orElseThrow(() -> new DispatcherRequestFailedException("Could not create task in dispatcher. Dispatcher did not return an id.")));
    }

    /**
     * Updates a task in the dispatcher.
     * @param taskAssignmentDTO the task to update
     * @throws MissingParameterException if not enough parameters have been provided to update a task
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     */
    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        if(StringUtils.isBlank(taskAssignmentDTO.getDatalogSolution()) ||
            StringUtils.isBlank(taskAssignmentDTO.getDatalogQuery())){
            throw new MissingParameterException("DatalogSolution or DatalogQuery is missing");
        }
        // Initialize DTO from task assignment
        var exercise = constructDatalogExerciseDtoFromAssignmentDto(taskAssignmentDTO);

        // Proxy request to dispatcher
        datalogClient.modifyDLGExercise(exercise, Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()));
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
            datalogClient.deleteDLGExercise(id);
        }catch(NumberFormatException | DispatcherRequestFailedException ignore){
            // ignore exception, as we do not want to throw an exception if deletion fails
        }

    }

    // Private region

    /**
     * Sets the link to the facts in the description of the task group.
     * @param dispatcherTaskGroupId the id of the task group in the dispatcher
     * @param newTaskGroupDTO the task group
     */
    private void setDispatcherLinkInTaskGroupDescription(String dispatcherTaskGroupId, NewTaskGroupDTO newTaskGroupDTO) {
        // Update description of task group with link to the facts
        String link = "<br> <a href='" + properties.getDispatcher().getDatalogFactsUrlPrefix() + dispatcherTaskGroupId + "' target='_blank'>Facts</a>";
        newTaskGroupDTO.setDescription(newTaskGroupDTO.getDescription() != null ? newTaskGroupDTO.getDescription() + link : link);
    }

    /**
     * Proxies the request to create a task group to the dispatcher.
     * @param datalogTaskGroupDTO the task group to create
     * @return the id of the task group in the dispatcher
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     */
    private Integer proxyTaskGroupCreationRequestToDispatcher(DatalogTaskGroupDTO datalogTaskGroupDTO) throws DispatcherRequestFailedException {
        return datalogClient.createDLGTaskGroup(datalogTaskGroupDTO);
    }

    /**
     * Constructs a {@link DatalogTaskGroupDTO} from a {@link NewTaskGroupDTO}.
     * @param newTaskGroupDTO the task group to construct from
     * @return the constructed task group
     */
    private DatalogTaskGroupDTO constructDatalogTaskGroupDto(NewTaskGroupDTO newTaskGroupDTO) {
        var group = new DatalogTaskGroupDTO();
        group.setFacts(newTaskGroupDTO.getDatalogFacts());
        group.setName(newTaskGroupDTO.getName());
        return group;
    }

    /**
     * Handles the creation of a task in the dispatcher.
     *
     *
     * @param newTaskAssignmentDTO the task to create
     * @return the id of the task in the dispatcher
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     */
    private Optional<Integer> handleTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        DatalogExerciseDTO exerciseDTO = constructDatalogExerciseDtoFromAssignmentDto(newTaskAssignmentDTO);
        var response = datalogClient.createDLGExercise(exerciseDTO);
        return Optional.of(response);
    }

    /**
     * Constructs a {@link DatalogExerciseDTO} from a {@link NewTaskAssignmentDTO}.
     * @param newTaskAssignmentDTO the task to construct from
     * @return the constructed task
     */
    private DatalogExerciseDTO constructDatalogExerciseDtoFromAssignmentDto(NewTaskAssignmentDTO newTaskAssignmentDTO) {
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

    /**
     * Checks if the task type fits the task group type.
     * @param newTaskAssignmentDTO the task assignment
     * @return true if the task type fits the task group type, false otherwise
     */
    private boolean taskTypeFitsTaskGroupType(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        // Fetch assigned task-group to check if the group-type matches the task type
        var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1));
        return group.filter(g -> g.getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString()))
            .isPresent();
    }
}
