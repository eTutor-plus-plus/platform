package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.dispatcher.*;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
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

    public DispatcherProxyService(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, DispatcherProxyResource proxyResource) {
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.proxyResource = proxyResource;
        this.mapper = new ObjectMapper();
    }

    /**
     * Returns the DispatcherSubmissionDTO for a given id
     *
     * @param UUID the UUID
     * @return the submission
     * @throws JsonProcessingException if the returned value cannot be deserialized
     */
    public DispatcherSubmissionDTO getSubmission(String UUID) throws JsonProcessingException {
        return mapper.readValue(proxyResource.getSubmission(UUID).getBody(), DispatcherSubmissionDTO.class);
    }

    /**
     * Returns the grading for a given id
     *
     * @param UUID the UUID
     * @return the grading
     * @throws JsonProcessingException if the returned value cannot be parsed
     */
    public DispatcherGradingDTO getGrading(String UUID) throws JsonProcessingException {
        return mapper.readValue(proxyResource.getGrading(UUID).getBody(), DispatcherGradingDTO.class);
    }

    /**
     * Adds task group related resources to the dispatcher
     *
     * @param newTaskGroupDTO the new task group
     */
    public int createTaskGroup(TaskGroupDTO newTaskGroupDTO) {
        if (newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())) {
            return proxyXMLtoDispatcher(newTaskGroupDTO);
        } else if (newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())) {
            return createSQLTaskGroup(newTaskGroupDTO);
        } else if (newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString())){
            return createDLGTaskGroup(newTaskGroupDTO);
        }
        return 200;
    }

    /**
     * Creates/updates a datalog by sending the fact-base to the dispatcher
     * @param newTaskGroupDTO the task group
     */
    private int createDLGTaskGroup(TaskGroupDTO newTaskGroupDTO) {
        Objects.requireNonNull(newTaskGroupDTO);
        int id = assignmentSPARQLEndpointService.getDispatcherIdForTaskGroup(newTaskGroupDTO);
        int statusCode;
        if (id != -1) {
            return proxyResource.updateDLGTaskGroup(id, newTaskGroupDTO.getDatalogFacts()).getStatusCodeValue();
        }

        var group = new DatalogTaskGroupDTO();
        group.setFacts(newTaskGroupDTO.getDatalogFacts());
        group.setName(newTaskGroupDTO.getName());

        Integer body = null;
        try {
            var response = proxyResource.createDLGTaskGroup(new ObjectMapper().writeValueAsString(group));
            body = response.getBody();
            statusCode = response.getStatusCodeValue();
        } catch (JsonProcessingException e) {
            return 500;
        }
        id = body != null ? body : -1;
        if(id == -1) return statusCode;
        assignmentSPARQLEndpointService.setDispatcherIdForTaskGroup(newTaskGroupDTO, id);
        return statusCode;

    }

    /**
     * Adds task group related resources for an SQL task group
     *
     * @param newTaskGroupDTO the task group
     */
    private int createSQLTaskGroup(TaskGroupDTO newTaskGroupDTO) {
        if (newTaskGroupDTO.getSqlCreateStatements() == null) return 500;

        SqlDataDefinitionDTO body = new SqlDataDefinitionDTO();
        body.setCreateStatements(Arrays.stream(newTaskGroupDTO.getSqlCreateStatements().trim().split(";")).toList());
        body.setInsertStatementsDiagnose(Arrays.stream(newTaskGroupDTO.getSqlInsertStatementsDiagnose().trim().split(";")).toList());
        body.setInsertStatementsSubmission(Arrays.stream(newTaskGroupDTO.getSqlInsertStatementsSubmission().trim().split(";")).toList());
        body.setSchemaName(newTaskGroupDTO.getName().trim().replace(" ", "_"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return 500;
        }
        return proxyResource.executeDDLForSQL(jsonBody).getStatusCodeValue();
    }

    /**
     * Sends the request to add the xml-files for an xquery task group to the DispatcherProxyResource
     *
     * @param taskGroupDTO the task group
     */
    private int proxyXMLtoDispatcher(TaskGroupDTO taskGroupDTO) {
        if (taskGroupDTO.getxQueryDiagnoseXML() == null || taskGroupDTO.getxQuerySubmissionXML() == null) return 400;

        String diagnoseXML = taskGroupDTO.getxQueryDiagnoseXML();
        String submissionXML = taskGroupDTO.getxQuerySubmissionXML();
        DispatcherXMLDTO body = new DispatcherXMLDTO(diagnoseXML, submissionXML);
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return 500;
        }
        var response = proxyResource.addXMLForXQTaskGroup(taskGroupDTO.getName().trim().replace(" ", "_"), jsonBody);
        var fileURL = response.getBody();
        assignmentSPARQLEndpointService.addXMLFileURL(taskGroupDTO, fileURL);
        return response.getStatusCodeValue();
    }

    /**
     * Triggers the deletion of task-group related resources by the dispatcher
     *
     * @param taskGroupDTO the task group
     */
    public void deleteDispatcherResourcesForTaskGroup(TaskGroupDTO taskGroupDTO) {
        Objects.requireNonNull(taskGroupDTO);
        Objects.requireNonNull(taskGroupDTO.getTaskGroupTypeId());
        Objects.requireNonNull(taskGroupDTO.getName());
        String taskGroupName = taskGroupDTO.getName().trim().replace(" ", "_");

        if (taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())) {
            proxyResource.deleteXMLofXQTaskGroup(taskGroupName);
        } else if (taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())) {
            proxyResource.deleteSQLSchema(taskGroupName);
            proxyResource.deleteSQLConnection(taskGroupName);
        } else if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString())){
            int id = assignmentSPARQLEndpointService.getDispatcherIdForTaskGroup(taskGroupDTO);
            if(id != -1) proxyResource.deleteDLGTaskGroup(id);
        }
    }

    /**
     * Handles the Dispatcher-related tasks for the creation of a new task assignment
     *
     * @param newTaskAssignmentDTO the task assignment
     * @throws JsonProcessingException if there is an error while serializing
     */
    public NewTaskAssignmentDTO createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws JsonProcessingException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskAssignmentTypeId());

        if (newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())) {
            if (newTaskAssignmentDTO.getTaskIdForDispatcher() == null && StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId())) {
                int id = this.createXQueryTask(newTaskAssignmentDTO);
                if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
            } else if(newTaskAssignmentDTO.getTaskIdForDispatcher() != null) {
                XQueryExerciseDTO e = this.getXQExerciseInfo(newTaskAssignmentDTO.getTaskIdForDispatcher());
                newTaskAssignmentDTO.setxQuerySolution(e.getQuery());
                if (!e.getSortedNodes().isEmpty())
                    newTaskAssignmentDTO.setxQueryXPathSorting(e.getSortedNodes().get(0));
            }
        } else if (newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())) {
            if (newTaskAssignmentDTO.getTaskIdForDispatcher() == null && StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId())) {
                int id = this.createSQLTask(newTaskAssignmentDTO);
                if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
            } else if(newTaskAssignmentDTO.getTaskIdForDispatcher()  != null) {
                String solution = fetchSQLSolution(newTaskAssignmentDTO.getTaskIdForDispatcher());
                newTaskAssignmentDTO.setSqlSolution(solution);
            }
        } else if(newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.DatalogTask.toString())){
            if (newTaskAssignmentDTO.getTaskIdForDispatcher() == null && StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId())) {
                int id = this.createDLGTask(newTaskAssignmentDTO);
                if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
            } else if(newTaskAssignmentDTO.getTaskIdForDispatcher()  != null) {
                DatalogExerciseDTO exerciseDTO = fetchDLGExerciseInfo (newTaskAssignmentDTO.getTaskIdForDispatcher());
                newTaskAssignmentDTO.setDatalogSolution(exerciseDTO.getSolution());
                newTaskAssignmentDTO.setDatalogQuery(exerciseDTO.getQueries().get(0));
                String uncheckedTerms = exerciseDTO.getUncheckedTerms()
                        .stream().map(DatalogTermDescriptionDTO::toString).reduce("", (x, y)->x+y+".\n");
                newTaskAssignmentDTO.setDatalogUncheckedTerms(uncheckedTerms);
            }
        }
        return newTaskAssignmentDTO;
    }

    private DatalogExerciseDTO fetchDLGExerciseInfo(String taskIdForDispatcher) {
        return proxyResource.getDLGExercise(Integer.parseInt(taskIdForDispatcher)).getBody();
    }

    private int createDLGTask(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        var exerciseDTO = getDatalogExerciseDTOFromTaskAssignment(newTaskAssignmentDTO);
        var response = proxyResource.createDLGExercise(exerciseDTO);
        if(response.getBody() != null) return response.getBody();
        else return -1;
    }

    /**
     * Takes a task assignment and initializes a Datalog exercise dto with it
     * @param newTaskAssignmentDTO the {@link NewTaskAssignmentDTO}
     * @return the {@link DatalogExerciseDTO}
     */
    private DatalogExerciseDTO getDatalogExerciseDTOFromTaskAssignment(NewTaskAssignmentDTO newTaskAssignmentDTO){
        DatalogExerciseDTO exerciseDTO = new DatalogExerciseDTO();
        List<String> queries = new ArrayList<>();
        // TODO: allow for multiple queries
        queries.add(newTaskAssignmentDTO.getDatalogQuery());
        exerciseDTO.setQueries(queries);
        exerciseDTO.setSolution(newTaskAssignmentDTO.getDatalogSolution());
        if(newTaskAssignmentDTO.getDatalogUncheckedTerms() != null) exerciseDTO.setUncheckedTerms(parseUncheckedTerms(newTaskAssignmentDTO.getDatalogUncheckedTerms()));
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
     * Creates an XQ-exercise
     *
     * @param newTaskAssignmentDTO the task assignment
     * @return the id of the created task
     */
    private int createXQueryTask(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        Objects.requireNonNull(newTaskAssignmentDTO.getxQuerySolution());

        List<String> sortings = new ArrayList<>();
        String dtoSorting = newTaskAssignmentDTO.getxQueryXPathSorting();
        if (dtoSorting != null) sortings.add(dtoSorting);

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
        var response = proxyResource.createXQExercise(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1).trim().replace(" ", "_"), jsonBody);
        return response.getBody();
    }

    /**
     * Returns XQ exercise information for a given id
     *
     * @param taskIdForDispatcher the task id
     * @return an XQueryExerciseDTO
     */
    private XQueryExerciseDTO getXQExerciseInfo(String taskIdForDispatcher) throws JsonProcessingException {
        return mapper.readValue(proxyResource.getXQExerciseInfo(Integer.parseInt(taskIdForDispatcher)).getBody(), XQueryExerciseDTO.class);
    }

    /**
     * Creates an SQL exercise
     *
     * @param newTaskAssignmentDTO the new task assignment
     * @return the id of the created exercise
     */
    private int createSQLTask(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        Objects.requireNonNull(newTaskAssignmentDTO.getSqlSolution());
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskGroupId());

        String solution = newTaskAssignmentDTO.getSqlSolution();
        String taskGroup = newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1).trim().replace(" ", "_");
        return Integer.parseInt(proxyResource.createSQLExercise(solution, taskGroup).getBody());
    }

    /**
     * Returns the solution for a given SQL-exercise-id
     *
     * @param taskIdForDispatcher the id
     * @return the solution
     */
    private String fetchSQLSolution(String taskIdForDispatcher) {
        return proxyResource.getSQLSolution(Integer.parseInt(taskIdForDispatcher)).getBody();

    }

    /**
     * Updates an exercise
     *
     * @param taskAssignmentDTO the task assignment to be updated
     */
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) {
        Objects.requireNonNull(taskAssignmentDTO);
        Objects.requireNonNull(taskAssignmentDTO.getTaskAssignmentTypeId());
        if(StringUtils.isBlank(taskAssignmentDTO.getTaskIdForDispatcher())) return;

        if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getxQuerySolution())){
                updateXQExercise(taskAssignmentDTO);
            }
        }else if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getSqlSolution())){
                updateSQLExercise(taskAssignmentDTO);
            }
        }else if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.DatalogTask.toString())){
            updateDLGExercise(taskAssignmentDTO);
        }
    }


    /**
     * Updates the dispatcher resources for a datalog-type task-assignment
     * @param taskAssignmentDTO the {@link TaskAssignmentDTO} to be updated
     */
    private void updateDLGExercise(TaskAssignmentDTO taskAssignmentDTO) {
        var exercise = getDatalogExerciseDTOFromTaskAssignment(taskAssignmentDTO);
        var response = proxyResource.modifyDLGExercise(exercise, Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()));
        if(response.getStatusCodeValue() != 200){
        }
    }

    /**
     * Updates an SQL Exercise
     *
     * @param taskAssignmentDTO the SQL task assignment to be updated
     */
    private void updateSQLExercise(TaskAssignmentDTO taskAssignmentDTO) {
        String solution = taskAssignmentDTO.getSqlSolution();
        int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
        proxyResource.updateSQLExerciseSolution(id, solution);
    }

    /**
     * Updates solution and sortings-XPath for an XQ-exercise
     *
     * @param taskAssignmentDTO the task assignment
     */
    private void updateXQExercise(TaskAssignmentDTO taskAssignmentDTO) {
        List<String> sortings = new ArrayList<>();
        String dtoSorting = taskAssignmentDTO.getxQueryXPathSorting();
        if (dtoSorting != null) sortings.add(dtoSorting);

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
     *
     * @param taskAssignmentDTO the task assignment to be deleted
     * @return
     */
    public void deleteTaskAssignment(TaskAssignmentDTO taskAssignmentDTO) {
        String taskType = taskAssignmentDTO.getTaskAssignmentTypeId();

        if (taskAssignmentDTO.getTaskIdForDispatcher() == null || taskType.equals(ETutorVocabulary.NoType.toString()) || taskType.equals(ETutorVocabulary.UploadTask.toString()))
            return;

        int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());

        if (taskType.equals(ETutorVocabulary.XQueryTask.toString())) {
            proxyResource.deleteXQExercise(id).getBody();
        } else if (taskType.equals(ETutorVocabulary.SQLTask.toString())) {
            proxyResource.deleteSQLExercise(id).getBody();
        }
    }


}

