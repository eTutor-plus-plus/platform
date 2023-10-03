package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.sql.SQLSchemaInfoDTO;
import at.jku.dke.etutor.objects.dispatcher.sql.SqlDataDefinitionDTO;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.tasktypes.proxy.SqlProxyService;
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

@Service
public class SqlService implements TaskTypeService, TaskGroupTypeService {
    private final SqlProxyService sqlProxyService;
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final ApplicationProperties properties;
    public SqlService(SqlProxyService sqlProxyService,
                      AssignmentSPARQLEndpointService assignmentSPARQLEndpointService,
                      ApplicationProperties properties) {
        this.sqlProxyService = sqlProxyService;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.properties = properties;
    }

    /**
     * Prepares the request body for the creation of a new SQL task group the dispatcher and sends it to the dispatcher.
     * If the request was successful, the task group description is updated with links to the tables and the schema info.
     * The id received from the dispatcher of the task group is also set in the RDF-Graph.
     * If the request was not successful, the task group is not created.
     * @param newTaskGroupDTO the task group to be created
     * @param isNew true if the task group is new, false if it already exists
     * @throws MissingParameterException if neither the insert statements for submission nor the insert statements for diagnose are set
     * @throws DispatcherRequestFailedException if the request to the dispatcher failed
     */
    @Override
    public void createOrUpdateTaskGroup(TaskGroupDTO newTaskGroupDTO, boolean isNew) throws MissingParameterException, DispatcherRequestFailedException {
        // Check if both insert statements are blank (if only one is blank, the dispatcher will use the other)
        if (StringUtils.isBlank(newTaskGroupDTO.getSqlInsertStatementsSubmission())
            && StringUtils.isBlank(newTaskGroupDTO.getSqlInsertStatementsDiagnose())) {
            throw new MissingParameterException();
        }

        // Initialize DTO
        SqlDataDefinitionDTO body = new SqlDataDefinitionDTO();

        String schemaName = newTaskGroupDTO.getName().trim().replace(" ", "_");
        body.setSchemaName(schemaName);

        List<String> createStatements = Arrays.stream(newTaskGroupDTO.getSqlCreateStatements().trim().split(";")).filter(StringUtils::isNotBlank).toList();
        body.setCreateStatements(createStatements);

        List<String> insertStatements;

        if (newTaskGroupDTO.getSqlInsertStatementsDiagnose() != null) {
            insertStatements = Arrays
                .stream(newTaskGroupDTO.getSqlInsertStatementsDiagnose().trim().split(";"))
                .filter(StringUtils::isNotBlank)
                .toList();
            body.setInsertStatementsDiagnose(insertStatements);
        }
        if (newTaskGroupDTO.getSqlInsertStatementsSubmission() != null) {
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
            throw new DispatcherRequestFailedException(e.getMessage());
        }
        var response = sqlProxyService.executeDDLForSQL(jsonBody);
        var statusCode = response.getStatusCodeValue();

        // Update task group description with links and schema info and set id from dispatcher in RDF-Graph
        if (statusCode == 200) { // update successful
            try {
                var schemaInfo = new ObjectMapper().readValue(response.getBody(), SQLSchemaInfoDTO.class);
                updateSQLTaskGroupDescriptionWithLinksAndSchemaInfo(schemaInfo, newTaskGroupDTO, isNew);
                if (schemaInfo.getDiagnoseConnectionId() != -1)
                    assignmentSPARQLEndpointService.setDispatcherIdForTaskGroup(newTaskGroupDTO, schemaInfo.getDiagnoseConnectionId());
            } catch (JsonProcessingException e) {
                throw new DispatcherRequestFailedException(e.getMessage());
            }
        }
    }

    /**
     * Deletes the SQL schema and connection of the task group from the dispatcher.
     * @param taskGroupDTO the task group to be deleted
     * @throws DispatcherRequestFailedException if the request to the dispatcher failed
     */
    @Override
    public void deleteTaskGroup(TaskGroupDTO taskGroupDTO) throws DispatcherRequestFailedException {
        if (!taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString()))
            return;

        String taskGroupName = taskGroupDTO.getName().trim().replace(" ", "_");
        sqlProxyService.deleteSQLSchema(taskGroupName);
        sqlProxyService.deleteSQLConnection(taskGroupName);
    }

    /**
     * Creates an SQL task by sending a request to the dispatcher.
     * If the request was successful, the id received from the dispatcher is set in the RDF-Graph.
     * @param newTaskAssignmentDTO the task assignment to be created
     * @throws MissingParameterException if the task group id or the solution is not set
     * @throws DispatcherRequestFailedException if the request to the dispatcher failed
     * @throws NotAValidTaskGroupException if the task group of the task assignment is not an SQL task group
     */
    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException, NotAValidTaskGroupException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString())
            && !newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString()))
            return;

        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskGroupId()) && StringUtils.isNotBlank(newTaskAssignmentDTO.getSqlSolution())) {
            // Fetch group to compare type
            var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1));
            group.filter(g -> g.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString()))
                .orElseThrow(NotAValidTaskGroupException::new);

            // Create task
            var optId = this.handleTaskCreation(newTaskAssignmentDTO);

            // Set dispatcher id of task
            optId.map(String::valueOf).ifPresent(newTaskAssignmentDTO::setTaskIdForDispatcher);
        } else {
            throw new MissingParameterException("Either the task group id or the solution is not set");
        }
    }

    /**
     * Updates an SQL task by sending a request to the dispatcher.
     *
     * @param taskAssignmentDTO the task assignment to be updated
     * @throws MissingParameterException if the solution is not set
     * @throws DispatcherRequestFailedException if the request to the dispatcher failed
     */
    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        if(StringUtils.isNotBlank(taskAssignmentDTO.getSqlSolution())){
            String solution = taskAssignmentDTO.getSqlSolution();
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            sqlProxyService.updateSQLExerciseSolution(id, solution);
        }else{
            throw new MissingParameterException("SqlSolution is missing");
        }
    }

    /**
     * Deletes an SQL task by sending a request to the dispatcher.
     *
     * @param taskAssignmentDTO the task assignment to be deleted
     * @throws DispatcherRequestFailedException does not happen as we ignore it if deletion is not successful
     */
    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            sqlProxyService.deleteSQLExercise(id);
        }catch(NumberFormatException | DispatcherRequestFailedException ignore){
            // we ignore it if deletion is not successful
        }

    }

    /**
     * Updates the description of an SQL task group by appending links
     * to the specified tables and information about the schema of the tables
     *
     * @param newTaskGroupDTO the {@link TaskGroupDTO}
     */
    private void updateSQLTaskGroupDescriptionWithLinksAndSchemaInfo(SQLSchemaInfoDTO info, TaskGroupDTO newTaskGroupDTO, boolean isNew) {
        // Check if schema info or connection id is invalid
        if (info.getDiagnoseConnectionId() == -1 || info.getTableColumns().isEmpty()) return;

        // Transform the name of the tables into links for group description
        var links = new ArrayList<String>();

        for (String table : info.getTableColumns().keySet()) {
            String link = "<a href='" + properties.getDispatcher().getSqlTableUrlPrefix() + table + "?connId=" + info.getDiagnoseConnectionId() + "' target='_blank'>" + table + "</a>";
            link += " (";
            link += String.join(", ", info.getTableColumns().get(table));
            link += ")";
            links.add(link);
        }

        // Update/Set description
        String description = newTaskGroupDTO.getDescription() != null ? newTaskGroupDTO.getDescription() : "";
        String startOfLinks = "<strong>Tables:";
        if (!isNew) {
            int indexOfTableLinks = description.indexOf(startOfLinks);
            if (indexOfTableLinks != -1) description = description.substring(0, indexOfTableLinks);
        }
        description += "<br>";
        description += startOfLinks + "</strong><br>";

        StringBuilder sb = new StringBuilder();
        for (String link : links) {
            sb.append(link).append("<br>");
        }
        description += sb.toString();

        // Set updated description and update group in RDF-graph to reflect the changes
        newTaskGroupDTO.setDescription(description);
        assignmentSPARQLEndpointService.modifyTaskGroup(newTaskGroupDTO);
    }

    /**
     * Creates an SQL exercise
     *
     * @param newTaskAssignmentDTO the new task assignment
     * @return the id of the created exercise
     */
    private Optional<Integer> handleTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        Objects.requireNonNull(newTaskAssignmentDTO.getSqlSolution());
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskGroupId());

        // Get solution and task group required by the dispatcher to create the task
        String solution = newTaskAssignmentDTO.getSqlSolution();
        String taskGroup = newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1).trim().replace(" ", "_");

        // Proxy request to dispatcher
        var response = sqlProxyService.createSQLExercise(solution, taskGroup);

        // Return dispatcher-id of the exercise
        try{
            return response.getBody() != null ? Optional.of(Integer.parseInt(response.getBody())) : Optional.empty();
        }catch(NumberFormatException ignored){
            throw new DispatcherRequestFailedException("Dispatcher returned invalid id");
        }
    }
}
