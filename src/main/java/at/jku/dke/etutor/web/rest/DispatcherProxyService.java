package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.dispatcher.*;
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
    private final ApplicationProperties properties;

    public DispatcherProxyService(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, DispatcherProxyResource proxyResource, ApplicationProperties properties) {
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.proxyResource = proxyResource;
        this.mapper = new ObjectMapper();
        this.properties = properties;
    }

    /**
     * Returns the {@link DispatcherSubmissionDTO} for a given id, which represents a submission for an individual task
     *
     * @param UUID the UUID
     * @return the submission
     * @throws JsonProcessingException if the returned value cannot be deserialized
     */
    public DispatcherSubmissionDTO getSubmission(String UUID) throws JsonProcessingException, DispatcherRequestFailedException {
        return mapper.readValue(proxyResource.getSubmission(UUID).getBody(), DispatcherSubmissionDTO.class);
    }

    /**
     * Returns the {@link DispatcherGradingDTO} for a given id, representing a graded submission for an individual task
     *
     * @param UUID the UUID
     * @return the grading
     * @throws JsonProcessingException if the returned value cannot be parsed
     */
    public DispatcherGradingDTO getGrading(String UUID) throws JsonProcessingException, DispatcherRequestFailedException {
        return mapper.readValue(proxyResource.getGrading(UUID).getBody(), DispatcherGradingDTO.class);
    }

    /**
     * Adds task group related resources to the dispatcher
     *
     * @param newTaskGroupDTO the new task group
     * @return the HTML status code of the creation
     */
    public int createTaskGroup(TaskGroupDTO newTaskGroupDTO, boolean isNew) throws DispatcherRequestFailedException, MissingParameterException {
        int statusCode = 200;
        if (newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())) {
            statusCode = proxyXMLtoDispatcher(newTaskGroupDTO);
        } else if (newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())) {
            statusCode = createSQLTaskGroup(newTaskGroupDTO, isNew);
        } else if (newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString())){
            statusCode =  createDLGTaskGroup(newTaskGroupDTO);
        }
        if(statusCode != 200){
            throw new DispatcherRequestFailedException();
        }

        return statusCode;
    }

    /**
     * Creates/updates a datalog group by sending the fact-base to the dispatcher.
     * Adds the returned id from the dispatcher of the task-group to the task-group (rdf) (only once).
     * Adds the link to view the facts to the task-groups description (only once).
     * @param newTaskGroupDTO the task group
     */
    private int createDLGTaskGroup(TaskGroupDTO newTaskGroupDTO) throws DispatcherRequestFailedException {
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

        String link = "<br> <a href='"+properties.getDispatcher().getDatalogFactsUrlPrefix()+id+"' target='_blank'>Facts</a>";
        newTaskGroupDTO.setDescription(newTaskGroupDTO.getDescription() != null ? newTaskGroupDTO.getDescription()+link : link);
        assignmentSPARQLEndpointService.modifyTaskGroup(newTaskGroupDTO);
        return statusCode;
    }

    /**
     * Adds task group related resources for an SQL task group in the dispatcher.
     * Appends the links to the tables to the task group description
     *
     * @param newTaskGroupDTO the task group
     */
    private int createSQLTaskGroup(TaskGroupDTO newTaskGroupDTO, boolean isNew) throws DispatcherRequestFailedException, MissingParameterException {
        if (StringUtils.isBlank(newTaskGroupDTO.getSqlInsertStatementsSubmission())
            && StringUtils.isBlank(newTaskGroupDTO.getSqlInsertStatementsDiagnose())){
            throw new MissingParameterException();
        }
        SqlDataDefinitionDTO body = new SqlDataDefinitionDTO();

        String schemaName = newTaskGroupDTO.getName().trim().replace(" ", "_");
        body.setSchemaName(schemaName);

        List<String> createStatements = Arrays.stream(newTaskGroupDTO.getSqlCreateStatements().trim().split(";")).filter(StringUtils::isNotBlank).toList();
        body.setCreateStatements(createStatements);

        List<String> insertStatements;

        if(newTaskGroupDTO.getSqlInsertStatementsDiagnose() != null){
            insertStatements = Arrays
                .stream(newTaskGroupDTO.getSqlInsertStatementsDiagnose().trim().split(";"))
                .filter(StringUtils::isNotBlank)
                .toList();
            body.setInsertStatementsDiagnose(insertStatements);
        }
        if(newTaskGroupDTO.getSqlInsertStatementsSubmission() != null){
            insertStatements = Arrays
                .stream(newTaskGroupDTO.getSqlInsertStatementsSubmission().trim().split(";"))
                .filter(StringUtils::isNotBlank)
                .toList();
            body.setInsertStatementsSubmission(insertStatements);
        }

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return 500;
        }
        var response = proxyResource.executeDDLForSQL(jsonBody);
        var statusCode = response.getStatusCodeValue();

        if(statusCode == 200){
            try {
                var schemaInfo = new ObjectMapper().readValue(response.getBody(), SQLSchemaInfoDTO.class);
                updateSQLTaskGroupDescriptionWithLinksAndSchemaInfo(schemaInfo, newTaskGroupDTO, isNew);
                if(schemaInfo.getDiagnoseConnectionId() != -1)assignmentSPARQLEndpointService.setDispatcherIdForTaskGroup(newTaskGroupDTO, schemaInfo.getDiagnoseConnectionId());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return statusCode;
    }

    /**
     *
     * Updates the description of an SQL task group by appending links to the specified tables and information about the schema of the tables
     * @param newTaskGroupDTO the {@link TaskGroupDTO}
     */
    private void updateSQLTaskGroupDescriptionWithLinksAndSchemaInfo(SQLSchemaInfoDTO info, TaskGroupDTO newTaskGroupDTO,boolean isNew) {
        if(info.getDiagnoseConnectionId() == -1 || info.getTableColumns().isEmpty()) return;
        var links = new ArrayList<String>();

        for(String table : info.getTableColumns().keySet()){
            String link = "<a href='"+properties.getDispatcher().getSqlTableUrlPrefix()+table+"?connId="+info.getDiagnoseConnectionId() +"' target='_blank'>"+table+"</a>";
            link += " (";
            link += String.join(", ",info.getTableColumns().get(table));
            link += ")";
            links.add(link);
        }
        String description = newTaskGroupDTO.getDescription() != null ? newTaskGroupDTO.getDescription() : "";
        String startOfLinks = "<strong>Tables:";
        if(!isNew){
            int indexOfTableLinks=description.indexOf(startOfLinks);
            if(indexOfTableLinks != -1) description=description.substring(0, indexOfTableLinks);
        }
        description += "<br>";
        description += startOfLinks + "</strong><br>";

        StringBuilder sb = new StringBuilder();
        for (String link : links){
            sb.append(link).append("<br>");
        }
        description += sb.toString();

        newTaskGroupDTO.setDescription(description);
        assignmentSPARQLEndpointService.modifyTaskGroup(newTaskGroupDTO);
    }

    /**
     * Sends the request to add the xml-files for a xquery task group to the DispatcherProxyResource.
     * Adds the returned file-url to the task group, which is needed to reference the XML's from the task-queries.
     * Adds the link to view the diagnose-XML file to the task group's description.
     *
     * @param taskGroupDTO the task group
     */
    private int proxyXMLtoDispatcher(TaskGroupDTO taskGroupDTO) throws DispatcherRequestFailedException, MissingParameterException {
        if(StringUtils.isBlank(taskGroupDTO.getxQueryDiagnoseXML()) &&
            StringUtils.isBlank(taskGroupDTO.getxQuerySubmissionXML())){
            throw new MissingParameterException();
        }

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

        if(fileURL != null) {
            String oldDescription = taskGroupDTO.getDescription() != null ? taskGroupDTO.getDescription() : "";
            String startOfLinks = "<a href=\"/XML";
            int indexOfLinks= oldDescription.indexOf(startOfLinks);
            if(indexOfLinks != -1) oldDescription = oldDescription.substring(0, indexOfLinks);

            String link = "<br><br> <a href='"+properties.getDispatcher().getXqueryXmlFileUrlPrefix()+fileURL.substring(fileURL.contains("=") ? fileURL.indexOf("=") +1 : 0)+"' target='_blank'>View XML</a>";
            String newDescription = oldDescription + link;
            newDescription += "<br><br> You can include the XML document for this task group using the following function: <br>";
            newDescription += "<b> let $doc := doc('"+ fileURL + "') </b>";
            taskGroupDTO.setDescription(newDescription);
            assignmentSPARQLEndpointService.modifyTaskGroup(taskGroupDTO);
        }
        return response.getStatusCodeValue();
    }

    /**
     * Triggers the deletion of task-group related resources by the dispatcher
     *
     * @param taskGroupDTO the task group
     */
    public void deleteDispatcherResourcesForTaskGroup(TaskGroupDTO taskGroupDTO) throws DispatcherRequestFailedException {
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
    public NewTaskAssignmentDTO createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws JsonProcessingException, MissingParameterException, NotAValidTaskGroupException, DispatcherRequestFailedException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskAssignmentTypeId());
        /*
        1. check which task type
        2. check whether dispatcher id is set to reference an existing task or if a new task should be created (taskgroup != null and of right type, solution != null)
         */

        if (newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())) { // XQuery task
            if (newTaskAssignmentDTO.getTaskIdForDispatcher() == null && StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId())
                && StringUtils.isNotBlank(newTaskAssignmentDTO.getxQuerySolution())) { // No Dispatcher-ID set AND task group not null AND solution not null
                var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1));
                if(group.isPresent()){
                    if(!group.get().getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())) throw new NotAValidTaskGroupException();
                }
                // task group is of right type (XQuery task group)
                int id = this.createXQueryTask(newTaskAssignmentDTO);
                if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
            } else if(newTaskAssignmentDTO.getTaskIdForDispatcher() != null) { // Dispatcher ID set to reference existing task
                XQueryExerciseDTO e = this.getXQExerciseInfo(newTaskAssignmentDTO.getTaskIdForDispatcher());
                newTaskAssignmentDTO.setxQuerySolution(e.getQuery());
                if (!e.getSortedNodes().isEmpty())
                    newTaskAssignmentDTO.setxQueryXPathSorting(e.getSortedNodes().get(0));
            }else{ // Either is the id not set, or group + solution is not set
                throw new MissingParameterException();
            }
        } else if (newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())) {
            if (newTaskAssignmentDTO.getTaskIdForDispatcher() == null && StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId()) && StringUtils.isNotBlank(newTaskAssignmentDTO.getSqlSolution())) {
                var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1));
                if(group.isPresent()){
                    if(!group.get().getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())) throw new NotAValidTaskGroupException();
                }
                int id = this.createSQLTask(newTaskAssignmentDTO);
                if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
            } else if(newTaskAssignmentDTO.getTaskIdForDispatcher()  != null) {
                String solution = fetchSQLSolution(newTaskAssignmentDTO.getTaskIdForDispatcher());
                newTaskAssignmentDTO.setSqlSolution(solution);
            } else {
                throw new MissingParameterException();
            }
        } else if(newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.DatalogTask.toString())){
            if (newTaskAssignmentDTO.getTaskIdForDispatcher() == null && StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId())
                && StringUtils.isNotBlank(newTaskAssignmentDTO.getDatalogSolution()) && StringUtils.isNotBlank(newTaskAssignmentDTO.getDatalogQuery())) {
                var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1));
                if(group.isPresent()){
                    if(!group.get().getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString()))
                        throw new NotAValidTaskGroupException();
                }
                int id = this.createDLGTask(newTaskAssignmentDTO);
                if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
            } else if(newTaskAssignmentDTO.getTaskIdForDispatcher()  != null) {
                DatalogExerciseDTO exerciseDTO = fetchDLGExerciseInfo (newTaskAssignmentDTO.getTaskIdForDispatcher());

                if(exerciseDTO == null) throw new DispatcherRequestFailedException();

                newTaskAssignmentDTO.setDatalogSolution(exerciseDTO.getSolution());
                newTaskAssignmentDTO.setDatalogQuery(exerciseDTO.getQueries().get(0));
                String uncheckedTerms = exerciseDTO.getUncheckedTerms()
                        .stream().map(DatalogTermDescriptionDTO::toString).reduce("", (x, y)->x+y+".\n");
                newTaskAssignmentDTO.setDatalogUncheckedTerms(uncheckedTerms);
            }else{
                throw new MissingParameterException();
            }
        }
        return newTaskAssignmentDTO;
    }

    /**
     * Fetches the exercise information for a datalog exercise according to its id
     * @param taskIdForDispatcher the id
     * @return the {@link DatalogExerciseDTO} containing the information
     */
    private DatalogExerciseDTO fetchDLGExerciseInfo(String taskIdForDispatcher) throws DispatcherRequestFailedException {
        return proxyResource.getDLGExercise(Integer.parseInt(taskIdForDispatcher)).getBody();
    }

    /**
     * Creates a datalog-task in the dispatcher
     * @param newTaskAssignmentDTO the{@link NewTaskAssignmentDTO} to be created
     * @return the dispatcher-id of the task
     */
    private int createDLGTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        var exerciseDTO = getDatalogExerciseDTOFromTaskAssignment(newTaskAssignmentDTO);
        var response = proxyResource.createDLGExercise(exerciseDTO);
        if(response.getBody() != null) return response.getBody();
        else return -1;
    }

    /**
     * Utility method that takes a task assignment and initializes a {@link DatalogExerciseDTO} with the required information
     * @param newTaskAssignmentDTO the {@link NewTaskAssignmentDTO}
     * @return the {@link DatalogExerciseDTO}
     */
    private DatalogExerciseDTO getDatalogExerciseDTOFromTaskAssignment(NewTaskAssignmentDTO newTaskAssignmentDTO){
        DatalogExerciseDTO exerciseDTO = new DatalogExerciseDTO();
        List<String> queries = new ArrayList<>();
        Arrays.stream(newTaskAssignmentDTO.getDatalogQuery().split(";")).forEach(x -> queries.add(x.trim().replace("\r", "").replace("\n", "").replace(" ", "")));
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
    private int createXQueryTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
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
        return response.getBody() != null ? response.getBody() : -1;
    }

    /**
     * Returns XQ exercise information for a given id
     *
     * @param taskIdForDispatcher the task id
     * @return an XQueryExerciseDTO
     */
    private XQueryExerciseDTO getXQExerciseInfo(String taskIdForDispatcher) throws JsonProcessingException, DispatcherRequestFailedException {
        var response = proxyResource.getXQExerciseInfo(Integer.parseInt(taskIdForDispatcher));
        if(response != null){
            return mapper.readValue(response.getBody(), XQueryExerciseDTO.class);
        }
        throw new DispatcherRequestFailedException();
    }

    /**
     * Creates an SQL exercise
     *
     * @param newTaskAssignmentDTO the new task assignment
     * @return the id of the created exercise
     */
    private int createSQLTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        Objects.requireNonNull(newTaskAssignmentDTO.getSqlSolution());
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskGroupId());

        String solution = newTaskAssignmentDTO.getSqlSolution();
        String taskGroup = newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1).trim().replace(" ", "_");

        var response = proxyResource.createSQLExercise(solution, taskGroup);
        return response.getBody() != null ? Integer.parseInt(response.getBody()) : -1;
    }

    /**
     * Returns the solution for a given SQL-exercise-id
     *
     * @param taskIdForDispatcher the id
     * @return the solution
     */
    private String fetchSQLSolution(String taskIdForDispatcher) throws DispatcherRequestFailedException {
        return proxyResource.getSQLSolution(Integer.parseInt(taskIdForDispatcher)).getBody();

    }

    /**
     * Updates an exercise
     *
     * @param taskAssignmentDTO the task assignment to be updated
     */
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        Objects.requireNonNull(taskAssignmentDTO);
        Objects.requireNonNull(taskAssignmentDTO.getTaskAssignmentTypeId());
        if(StringUtils.isBlank(taskAssignmentDTO.getTaskIdForDispatcher())) {
            throw new MissingParameterException();
        }

        if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getxQuerySolution())){
                updateXQExercise(taskAssignmentDTO);
            }else{
                throw new MissingParameterException();
            }
        }else if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getSqlSolution())){
                updateSQLExercise(taskAssignmentDTO);
            }else{
                throw new MissingParameterException();
            }
        }else if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.DatalogTask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getDatalogSolution()) && StringUtils.isNotBlank(taskAssignmentDTO.getDatalogQuery())){
                updateDLGExercise(taskAssignmentDTO);
            }else{
                throw new MissingParameterException();
            }
        }
    }


    /**
     * Updates the dispatcher resources for a datalog-type task-assignment
     * @param taskAssignmentDTO the {@link TaskAssignmentDTO} to be updated
     */
    private void updateDLGExercise(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        var exercise = getDatalogExerciseDTOFromTaskAssignment(taskAssignmentDTO);
        var response = proxyResource.modifyDLGExercise(exercise, Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()));
        if(response == null || response.getStatusCodeValue() != 200) throw new DispatcherRequestFailedException();
    }

    /**
     * Updates an SQL Exercise
     *
     * @param taskAssignmentDTO the SQL task assignment to be updated
     */
    private void updateSQLExercise(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        String solution = taskAssignmentDTO.getSqlSolution();
        int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
        proxyResource.updateSQLExerciseSolution(id, solution);
    }

    /**
     * Updates an XQUery task in the dispatcher (the solution and the sortings-XPath)
     *
     * @param taskAssignmentDTO the task assignment to be updated
     */
    private void updateXQExercise(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
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
        var response = proxyResource.updateXQExercise(Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()), jsonBody);
        if (response == null || response.getStatusCodeValue() != 200){
            throw new DispatcherRequestFailedException();
        }
    }

    /**
     * Deletes a task assignment (exercise) in the dispatcher according to the task-type
     *
     * @param taskAssignmentDTO the task assignment to be deleted
     */
    public void deleteTaskAssignment(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        String taskType = taskAssignmentDTO.getTaskAssignmentTypeId();

        if (taskAssignmentDTO.getTaskIdForDispatcher() == null || taskType.equals(ETutorVocabulary.NoType.toString()) || taskType.equals(ETutorVocabulary.UploadTask.toString()))
            return;

        int id = -1;
        try{
            id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
        }catch(NumberFormatException e){
            return;
        }
        if (taskType.equals(ETutorVocabulary.XQueryTask.toString())) {
            proxyResource.deleteXQExercise(id);
        } else if (taskType.equals(ETutorVocabulary.SQLTask.toString())) {
            proxyResource.deleteSQLExercise(id);
        } else if (taskType.equals(ETutorVocabulary.DatalogTask.toString())){
            proxyResource.deleteDLGExercise(id);
        }
    }
}

