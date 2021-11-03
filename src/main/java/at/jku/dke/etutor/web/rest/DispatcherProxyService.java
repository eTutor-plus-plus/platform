package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.dispatcher.*;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.web.rest.DispatcherProxyResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Service to proxy requests to the dispatcher
 */
@Service
public class DispatcherProxyService {
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final DispatcherProxyResource proxyResource;
    private final ObjectMapper mapper;

    public DispatcherProxyService(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, DispatcherProxyResource proxyResource){
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.proxyResource = proxyResource;
        this.mapper = new ObjectMapper();
    }

    /**
     * Returns the DispatcherSubmissionDTO for a given id
     * @param UUID the UUID
     * @return the submission
     * @throws JsonProcessingException if the returned value cannot be deserialized
     */
    public DispatcherSubmissionDTO getSubmission(String UUID) throws JsonProcessingException {
        return  mapper.readValue(proxyResource.getSubmission(UUID).getBody(), DispatcherSubmissionDTO.class);
    }

    /**
     * Returns the grading for a given id
     * @param UUID the UUID
     * @return the grading
     * @throws JsonProcessingException if the returned value cannot be parsed
     */
    public DispatcherGradingDTO getGrading(String UUID) throws JsonProcessingException {
        return mapper.readValue(proxyResource.getGrading(UUID).getBody(), DispatcherGradingDTO.class);
    }

    /**
     * Adds task group related resources to the dispatcher
     * @param newTaskGroupDTO the new task group
     */
    public void createTaskGroup(TaskGroupDTO newTaskGroupDTO) {
        if(newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())){
            proxyXMLtoDispatcher(newTaskGroupDTO);
        }else if(newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())){
            createSQLTaskGroup(newTaskGroupDTO);
        }
    }

    /**
     * Adds task group related resources for an SQL task group
     * @param newTaskGroupDTO the task group
     */
    private void createSQLTaskGroup(TaskGroupDTO newTaskGroupDTO) {
        Objects.requireNonNull(newTaskGroupDTO.getSqlCreateStatements());
        Objects.requireNonNull(newTaskGroupDTO.getSqlInsertStatementsDiagnose());
        Objects.requireNonNull(newTaskGroupDTO.getSqlInsertStatementsSubmission());

        SqlDataDefinitionDTO body = new SqlDataDefinitionDTO();
        body.setCreateStatements(Arrays.stream(newTaskGroupDTO.getSqlCreateStatements().trim().split(";")).toList());
        body.setInsertStatementsDiagnose(Arrays.stream(newTaskGroupDTO.getSqlInsertStatementsDiagnose().trim().split(";")).toList());
        body.setInsertStatementsSubmission(Arrays.stream(newTaskGroupDTO.getSqlInsertStatementsSubmission().trim().split(";")).toList());
        body.setSchemaName(newTaskGroupDTO.getName());

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        proxyResource.executeDDLForSQL(jsonBody);

    }
    /**
     * Sends the request to add the xml-files for an xquery task group to the DispatcherProxyResource
     * @param taskGroupDTO the task group
     */
    private void proxyXMLtoDispatcher(TaskGroupDTO taskGroupDTO) {
        Objects.requireNonNull(taskGroupDTO.getId());
        Objects.requireNonNull(taskGroupDTO.getxQueryDiagnoseXML());
        Objects.requireNonNull(taskGroupDTO.getxQuerySubmissionXML());

        String diagnoseXML = taskGroupDTO.getxQueryDiagnoseXML();
        String submisisonXML = taskGroupDTO.getxQuerySubmissionXML();
        DispatcherXMLDTO body = new DispatcherXMLDTO(diagnoseXML,submisisonXML);
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        var response = proxyResource.addXMLForXQTaskGroup(taskGroupDTO.getName(), jsonBody);
        var fileURL = response.getBody();
        assignmentSPARQLEndpointService.addXMLFileURL(taskGroupDTO, fileURL);
    }

    /**
     * Triggers the deletion of task-group related resources by the dispatcher
     * @param taskGroupDTO  the task group
     */
    public void deleteDispatcherResourcesForTaskGroup(TaskGroupDTO taskGroupDTO) {
        Objects.requireNonNull(taskGroupDTO);
        Objects.requireNonNull(taskGroupDTO.getTaskGroupTypeId());
        Objects.requireNonNull(taskGroupDTO.getName());

        if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())){
            proxyResource.deleteXMLofXQTaskGroup(taskGroupDTO.getName());
        }else if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())){
            proxyResource.deleteSQLSchema(taskGroupDTO.getName());
            proxyResource.deleteSQLConnection(taskGroupDTO.getName());
        }
    }

    /**
     * Handles the Dispatcher-related tasks for the creation of a new task assignment
     * @param newTaskAssignmentDTO the task assignment
     * @throws JsonProcessingException if there is an error while serializing
     */
    public NewTaskAssignmentDTO createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws JsonProcessingException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskAssignmentTypeId());

        if(newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())){
            if(newTaskAssignmentDTO.getTaskIdForDispatcher() == null){
                int id = this.createXQueryTask(newTaskAssignmentDTO);
                if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id +"");
            }else{
                XQueryExerciseDTO e = this.getXQExerciseInfo(newTaskAssignmentDTO.getTaskIdForDispatcher());
                newTaskAssignmentDTO.setxQuerySolution(e.getQuery());
                if(!e.getSortedNodes().isEmpty())newTaskAssignmentDTO.setxQueryXPathSorting(e.getSortedNodes().get(0));
            }
        }else if(newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())){
            if(newTaskAssignmentDTO.getTaskIdForDispatcher() == null){
                int id = this.createSQLTask(newTaskAssignmentDTO);
                if(id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id+"");
            }else{
                String solution = fetchSQLSolution(newTaskAssignmentDTO.getTaskIdForDispatcher());
                newTaskAssignmentDTO.setSqlSolution(solution);
            }
        }
        return newTaskAssignmentDTO;
    }

    /**
     * Creates an XQ-exercise
     * @param newTaskAssignmentDTO the task assignment
     * @return the id of the created task
     */
    private int createXQueryTask(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        Objects.requireNonNull(newTaskAssignmentDTO.getxQuerySolution());

        List<String> sortings = new ArrayList<>();
        String dtoSorting = newTaskAssignmentDTO.getxQueryXPathSorting();
        if(dtoSorting != null) sortings.add(dtoSorting);

        XQueryExerciseDTO body = new XQueryExerciseDTO();
        body.setQuery(newTaskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return -1;
        }
        var response = proxyResource.createXQExercise(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1), jsonBody);
        return response.getBody();
    }

    /**
     * Returns XQ exercise information for a given id
     * @param taskIdForDispatcher the task id
     * @return an XQueryExerciseDTO
     */
    private XQueryExerciseDTO getXQExerciseInfo(String taskIdForDispatcher) throws JsonProcessingException {
        return mapper.readValue(proxyResource.getXQExerciseInfo(Integer.parseInt(taskIdForDispatcher)).getBody(), XQueryExerciseDTO.class);
    }

    /**
     * Creates an SQL exercise
     * @param newTaskAssignmentDTO the new task assignment
     * @return the id of the created exercise
     */
    private int createSQLTask(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        Objects.requireNonNull(newTaskAssignmentDTO.getSqlSolution());
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskGroupId());

        String solution = newTaskAssignmentDTO.getSqlSolution();
        String taskGroup = newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1);
        return Integer.parseInt(proxyResource.createSQLExercise(solution, taskGroup).getBody());
    }

    /**
     * Returns the solution for a given SQL-exercise-id
     * @param taskIdForDispatcher the id
     * @return the solution
     */
    private String fetchSQLSolution(String taskIdForDispatcher) {
        return proxyResource.getSQLSolution(Integer.parseInt(taskIdForDispatcher)).getBody();

    }

    /**
     * Updates an exercise
     * @param taskAssignmentDTO the task assignment to be updated
     */
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) {
        Objects.requireNonNull(taskAssignmentDTO);
        Objects.requireNonNull(taskAssignmentDTO.getTaskAssignmentTypeId());

        if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getxQuerySolution())
                && StringUtils.isNotBlank(taskAssignmentDTO.getTaskIdForDispatcher())){
                updateXQExercise(taskAssignmentDTO);
            }
        }else if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getSqlSolution()) && StringUtils.isNotBlank(taskAssignmentDTO.getTaskIdForDispatcher())){
                updateSQLExercise(taskAssignmentDTO);
            }
        }
    }

    /**
     * Updates an SQL Exercise
     * @param taskAssignmentDTO the SQL task assignment to be updated
     */
    private void updateSQLExercise(TaskAssignmentDTO taskAssignmentDTO) {
        String solution = taskAssignmentDTO.getSqlSolution();
        int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
        proxyResource.updateSQLExerciseSolution(id, solution);
    }

    /**
     * Updates solution and sortings-XPath for an XQ-exercise
     * @param taskAssignmentDTO the task assignment
     */
    private void updateXQExercise(TaskAssignmentDTO taskAssignmentDTO)  {
        List<String> sortings = new ArrayList<>();
        String dtoSorting = taskAssignmentDTO.getxQueryXPathSorting();
        if(dtoSorting != null) sortings.add(dtoSorting);

        XQueryExerciseDTO body = new XQueryExerciseDTO();
        body.setQuery(taskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        proxyResource.updateXQExercise(Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()), jsonBody).getBody();
    }

    /**
     * Deletes a task assignment (exercise) in the dispatcher
     * @param taskAssignmentDTO the task assignment to be deleted
     * @return
     */
    public void deleteTaskAssignment(TaskAssignmentDTO taskAssignmentDTO) {
        String taskType = taskAssignmentDTO.getTaskAssignmentTypeId();
        int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());

        if(taskType.equals(ETutorVocabulary.NoType.toString()) || taskType.equals(ETutorVocabulary.UploadTask.toString())) return;

        if(taskType.equals(ETutorVocabulary.XQueryTask.toString())){
            proxyResource.deleteXQExercise(id).getBody();
        }else if(taskType.equals(ETutorVocabulary.SQLTask.toString())){
          proxyResource.deleteSQLExercise(id).getBody();
        }
    }



}
