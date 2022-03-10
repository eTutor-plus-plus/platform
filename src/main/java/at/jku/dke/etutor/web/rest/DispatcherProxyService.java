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
        // Check if task group is already existent or new
        int id = assignmentSPARQLEndpointService.getDispatcherIdForTaskGroup(newTaskGroupDTO);
        int statusCode;
        if (id != -1) {
            return proxyResource.updateDLGTaskGroup(id, newTaskGroupDTO.getDatalogFacts()).getStatusCodeValue();
        }// group is new

        // Initialize DTO
        var group = new DatalogTaskGroupDTO();
        group.setFacts(newTaskGroupDTO.getDatalogFacts());
        group.setName(newTaskGroupDTO.getName());

        // Proxy request
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

        // Set received id for task group in RDF-graph
        assignmentSPARQLEndpointService.setDispatcherIdForTaskGroup(newTaskGroupDTO, id);

        // Update description of task group with link to the facts
        String link = "<br> <a href='"+properties.getDispatcher().getDatalogFactsUrlPrefix()+id+"' target='_blank'>Facts</a>";
        newTaskGroupDTO.setDescription(newTaskGroupDTO.getDescription() != null ? newTaskGroupDTO.getDescription()+link : link);

        // Update task group in RDF to include the new description
        assignmentSPARQLEndpointService.modifyTaskGroup(newTaskGroupDTO);

        // Return status code
        return statusCode;
    }

    /**
     * Adds task group related resources for an SQL task group in the dispatcher.
     * Appends the links to the tables to the task group description
     *
     * @param newTaskGroupDTO the task group
     */
    private int createSQLTaskGroup(TaskGroupDTO newTaskGroupDTO, boolean isNew) throws DispatcherRequestFailedException, MissingParameterException {
        // Check if both insert statements are blank (if only one is blank, the dispatcher will use the other)
        if (StringUtils.isBlank(newTaskGroupDTO.getSqlInsertStatementsSubmission())
            && StringUtils.isBlank(newTaskGroupDTO.getSqlInsertStatementsDiagnose())){
            throw new MissingParameterException();
        }

        // Initialize DTO
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

        // Proxy request to disptacher
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

        // Update task group description with links and schema info and set id from dispatcher in RDF-Graph
        if(statusCode == 200){ // update succesful
            try {
                var schemaInfo = new ObjectMapper().readValue(response.getBody(), SQLSchemaInfoDTO.class);
                updateSQLTaskGroupDescriptionWithLinksAndSchemaInfo(schemaInfo, newTaskGroupDTO, isNew);
                if(schemaInfo.getDiagnoseConnectionId() != -1)assignmentSPARQLEndpointService.setDispatcherIdForTaskGroup(newTaskGroupDTO, schemaInfo.getDiagnoseConnectionId());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // return status code
        return statusCode;
    }

    /**
     *
     * Updates the description of an SQL task group by appending links to the specified tables and information about the schema of the tables
     * @param newTaskGroupDTO the {@link TaskGroupDTO}
     */
    private void updateSQLTaskGroupDescriptionWithLinksAndSchemaInfo(SQLSchemaInfoDTO info, TaskGroupDTO newTaskGroupDTO,boolean isNew) {
        // Check if schema info or connection id is invalid
        if(info.getDiagnoseConnectionId() == -1 || info.getTableColumns().isEmpty()) return;

        // Transform the name of the tables into links for group description
        var links = new ArrayList<String>();

        for(String table : info.getTableColumns().keySet()){
            String link = "<a href='"+properties.getDispatcher().getSqlTableUrlPrefix()+table+"?connId="+info.getDiagnoseConnectionId() +"' target='_blank'>"+table+"</a>";
            link += " (";
            link += String.join(", ",info.getTableColumns().get(table));
            link += ")";
            links.add(link);
        }

        // Update/Set description
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

        // Set updated description and update group in RDF-graph to reflect the changes
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
        // Check if both XML´s are empty (if only one is empty, the dispatcher will use the other)
        if(StringUtils.isBlank(taskGroupDTO.getxQueryDiagnoseXML()) &&
            StringUtils.isBlank(taskGroupDTO.getxQuerySubmissionXML())){
            throw new MissingParameterException();
        }

        // Initialize DTO
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

        if(fileURL != null && StringUtils.isBlank(fileURL)){
            // Update file-url in RDF-Graph (can be used in XQuery-queries to reference the group/XML)
            assignmentSPARQLEndpointService.addXMLFileURL(taskGroupDTO, fileURL);

            // Update group-description with link to view the diagnose XML
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
        if(!isDispatcherTaskAssignment(newTaskAssignmentDTO)) return newTaskAssignmentDTO;

        if (newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())) { // XQuery task
            handleXQTaskCreation(newTaskAssignmentDTO);
        } else if (newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())) {
            handleSQLTaskCreation(newTaskAssignmentDTO);
        } else if(newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.DatalogTask.toString())){
            handleDLGTaskCreation(newTaskAssignmentDTO);
        }
        return newTaskAssignmentDTO;
    }

    /**
     * Handles creation of a datalog task
     * @param newTaskAssignmentDTO the task assignment
     * @throws NotAValidTaskGroupException if task group type does not match the task type
     * @throws DispatcherRequestFailedException if dispatche returned an error
     * @throws MissingParameterException if not enough parameter have been provided to execute the creation
     */
    private void handleDLGTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws NotAValidTaskGroupException, DispatcherRequestFailedException, MissingParameterException {
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.DatalogTask.toString())) return;

        // Check whether we want to create a new task or if we are referencing a task which is already existing in the dispatcher by referencing via the ID
        if (newTaskAssignmentDTO.getTaskIdForDispatcher() == null && StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId())
            && StringUtils.isNotBlank(newTaskAssignmentDTO.getDatalogSolution()) && StringUtils.isNotBlank(newTaskAssignmentDTO.getDatalogQuery())) { //creation of new task

            // Fetch assigned task-group to check if the group-type matches the task type
            var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1));
            if(group.isPresent()){
                if(!group.get().getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString()))
                    throw new NotAValidTaskGroupException();
            }
            // Create task
            int id = this.createDLGTask(newTaskAssignmentDTO);

            // Set the returned id of the task
            if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
        } else if(newTaskAssignmentDTO.getTaskIdForDispatcher()  != null) { // Reference of existing task
            // Fetch the Info about the exercise from dispatcher
            DatalogExerciseDTO exerciseDTO = fetchDLGExerciseInfo (newTaskAssignmentDTO.getTaskIdForDispatcher());

            // Indicates most likely that id could not be found in the dispatcher
            if(exerciseDTO == null) throw new DispatcherRequestFailedException();

            // Set the relevant fields in the task assignment
            newTaskAssignmentDTO.setDatalogSolution(exerciseDTO.getSolution());
            newTaskAssignmentDTO.setDatalogQuery(exerciseDTO.getQueries().get(0));
            // Parse the List of DatalogUncheckedTerms into a String representation
            String uncheckedTerms = exerciseDTO.getUncheckedTerms()
                .stream().map(DatalogTermDescriptionDTO::toString).reduce("", (x, y)->x+y+".\n");
            newTaskAssignmentDTO.setDatalogUncheckedTerms(uncheckedTerms);
        }else{ // Creation failed, either because no id and no group has been set or, in the case of the creation of a new task, not enough info has been provided
            throw new MissingParameterException();
        }
    }

    /**
     * Handles the creation of an XQuery task
     * @param newTaskAssignmentDTO the task assignment
     * @throws NotAValidTaskGroupException if task group type does not match the task type
     * @throws DispatcherRequestFailedException if dispatcher returned an error
     * @throws MissingParameterException if not enough parameter have been provided to execute the creation
     */
    private void handleXQTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException, JsonProcessingException, MissingParameterException, NotAValidTaskGroupException {
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())) return;

        // Check wheter we are creating a new task, or referencing an existing task
        if (newTaskAssignmentDTO.getTaskIdForDispatcher() == null && StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId())
            && StringUtils.isNotBlank(newTaskAssignmentDTO.getxQuerySolution())) { // No Dispatcher-ID set AND task group not null AND solution not null

            // Fetch group to compare the group type with the task assignment type
            var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1));
            if(group.isPresent() && !group.get().getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())){
               throw new NotAValidTaskGroupException();
            }// task group is of right type (XQuery task group)

            // Create XQ-Task
            int id = this.createXQueryTask(newTaskAssignmentDTO);

            // Set the returned id from the task in the dispatcher
            if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
        } else if(newTaskAssignmentDTO.getTaskIdForDispatcher() != null) { // Dispatcher ID set to reference existing task
            // Fetch info about the exercise from dispatcher
            XQueryExerciseDTO e = this.getXQExerciseInfo(newTaskAssignmentDTO.getTaskIdForDispatcher());

            // Set the solution and optional sorted nodes
            newTaskAssignmentDTO.setxQuerySolution(e.getQuery());
            if (!e.getSortedNodes().isEmpty())
                newTaskAssignmentDTO.setxQueryXPathSorting(e.getSortedNodes().get(0));
        }else{ // Either is the id not set, or group + solution is not set
            throw new MissingParameterException();
        }
    }

    /**
     * Handles creation of an SQL-Task
     * @param newTaskAssignmentDTO the task assignment
     * @throws DispatcherRequestFailedException if an error is returned by the dispatcher
     * @throws NotAValidTaskGroupException if the task group type does not match the task type
     * @throws MissingParameterException if not enough parameters have been provided to create the task
     */
    private void handleSQLTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException, NotAValidTaskGroupException, MissingParameterException {
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString())) return;

        // Check wheter we are creating a new task or referencing an existing task in the dispatcher
        if (newTaskAssignmentDTO.getTaskIdForDispatcher() == null && StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId()) && StringUtils.isNotBlank(newTaskAssignmentDTO.getSqlSolution())) {
            // Fetch group to compare type
            var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1));
            if (group.isPresent()) {
                if (!group.get().getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString()))
                    throw new NotAValidTaskGroupException();
            }
            // Create task
            int id = this.createSQLTask(newTaskAssignmentDTO);

            // Set disptacher id of task
            if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
        } else if (newTaskAssignmentDTO.getTaskIdForDispatcher() != null) { // Reference of existing task
            // Fetch solution
            String solution = fetchSQLSolution(newTaskAssignmentDTO.getTaskIdForDispatcher());
            // Set solution
            newTaskAssignmentDTO.setSqlSolution(solution);
        } else {
            throw new MissingParameterException();
        }
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
     * Creates an XQ-exercise
     *
     * @param newTaskAssignmentDTO the task assignment
     * @return the id of the created task
     */
    private int createXQueryTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        Objects.requireNonNull(newTaskAssignmentDTO.getxQuerySolution());

        // Initialize DTO
        XQueryExerciseDTO body = new XQueryExerciseDTO();

        List<String> sortings = new ArrayList<>();
        String dtoSorting = newTaskAssignmentDTO.getxQueryXPathSorting();
        if (dtoSorting != null) sortings.add(dtoSorting);


        body.setQuery(newTaskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        // Send request
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return -1;
        }

        var response = proxyResource.createXQExercise(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1).trim().replace(" ", "_"), jsonBody);

        // Return ID of the task
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

        // Get solution and task group required by the dispatcher to create the task
        String solution = newTaskAssignmentDTO.getSqlSolution();
        String taskGroup = newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1).trim().replace(" ", "_");

        // Proxy request to dispatcher
        var response = proxyResource.createSQLExercise(solution, taskGroup);

        // Return dispatcher-id of the exercise
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
        if(!isDispatcherTaskAssignment(taskAssignmentDTO)) return;

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
     * Checks whether the given task assignment has a type related to the dispatcher
     * @param taskAssignmentDTO the task assignment
     * @return boolean indicating type
     */
    private boolean isDispatcherTaskAssignment(NewTaskAssignmentDTO taskAssignmentDTO) {
        String type = taskAssignmentDTO.getTaskAssignmentTypeId();
        return type.equals(ETutorVocabulary.SQLTask.toString()) ||
            type.equals(ETutorVocabulary.DatalogTask.toString()) ||
            type.equals(ETutorVocabulary.RATask.toString()) ||
            type.equals(ETutorVocabulary.XQueryTask.toString());
    }


    /**
     * Updates the dispatcher resources for a datalog-type task-assignment
     * @param taskAssignmentDTO the {@link TaskAssignmentDTO} to be updated
     */
    private void updateDLGExercise(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        // Initialize DTO from task assignment
        var exercise = getDatalogExerciseDTOFromTaskAssignment(taskAssignmentDTO);

        // Proxy request to dispatcher
        proxyResource.modifyDLGExercise(exercise, Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()));
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
        // Initialize DTO
        XQueryExerciseDTO body = new XQueryExerciseDTO();


        List<String> sortings = new ArrayList<>();
        String dtoSorting = taskAssignmentDTO.getxQueryXPathSorting();
        if (dtoSorting != null) sortings.add(dtoSorting);
        body.setQuery(taskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // Proxy request
        proxyResource.updateXQExercise(Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()), jsonBody);
    }

    /**
     * Deletes a task assignment (exercise) in the dispatcher according to the task-type
     *
     * @param taskAssignmentDTO the task assignment to be deleted
     */
    public void deleteTaskAssignment(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        String taskType = taskAssignmentDTO.getTaskAssignmentTypeId();
        if(!isDispatcherTaskAssignment(taskAssignmentDTO) || taskAssignmentDTO.getTaskIdForDispatcher() == null) return;

        // Parse the id of the task
        int id = -1;
        try{
            id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
        }catch(NumberFormatException e){
            return;
        }

        // Proxy request according to task type
        if (taskType.equals(ETutorVocabulary.XQueryTask.toString())) {
            proxyResource.deleteXQExercise(id);
        } else if (taskType.equals(ETutorVocabulary.SQLTask.toString())) {
            proxyResource.deleteSQLExercise(id);
        } else if (taskType.equals(ETutorVocabulary.DatalogTask.toString())){
            proxyResource.deleteDLGExercise(id);
        }
    }
}

