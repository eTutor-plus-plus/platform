package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.sql.SQLSchemaInfoDTO;
import at.jku.dke.etutor.objects.dispatcher.sql.SqlDataDefinitionDTO;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.client.dke.SqlClient;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.xml.validation.Schema;
import java.util.*;

/**
 * Service that handles all SQL task-type specific operations.
 */
@Service
public class SqlService implements TaskTypeService, TaskGroupTypeService {
    private final SqlClient sqlClient;
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final ApplicationProperties properties;
    public SqlService(SqlClient sqlClient,
                      AssignmentSPARQLEndpointService assignmentSPARQLEndpointService,
                      ApplicationProperties properties) {
        this.sqlClient = sqlClient;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.properties = properties;
    }

    /**
     * Creates the SQL schema and connection of the task group on the dispatcher.
     * @param newTaskGroupDTO the task group to be created
     * @throws MissingParameterException if neither the insert statements for submission nor the insert statements for diagnose are set
     * @throws DispatcherRequestFailedException if the request to the dispatcher failed
     */
    @Override
    public void createTaskGroup(NewTaskGroupDTO newTaskGroupDTO) throws TaskTypeSpecificOperationFailedException {
        createOrUpdateTaskGroup(newTaskGroupDTO, true);
    }

    /**
     * Updates the SQL schema and connection of the task group on the dispatcher.
      * @param taskGroupDTO the task group to be updated
     * @throws TaskTypeSpecificOperationFailedException if an error occurs during the update
     */
    @Override
    public void updateTaskGroup(TaskGroupDTO taskGroupDTO) throws TaskTypeSpecificOperationFailedException {
        createOrUpdateTaskGroup(taskGroupDTO, false);
    }


    /**
     * Deletes the SQL schema and connection of the task group from the dispatcher.
     * @param taskGroupDTO the task group to be deleted
     */
    @Override
    public void deleteTaskGroup(TaskGroupDTO taskGroupDTO) {
        if (!taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString()))
            return;

        String taskGroupName = taskGroupDTO.getName().trim().replace(" ", "_");
        try {
            sqlClient.deleteSQLSchema(taskGroupName);
            sqlClient.deleteSQLConnection(taskGroupName);
        } catch (DispatcherRequestFailedException ignore) {
            // we ignore this
        }
    }

    /**
     * Creates an SQL task by sending a request to the dispatcher.
     * If the request was successful, the id received from the dispatcher is set for the passed object.
     * @param newTaskAssignmentDTO the task assignment to be created
     * @throws MissingParameterException if the task group id or the solution is not set
     * @throws DispatcherRequestFailedException if the request to the dispatcher failed
     * @throws NotAValidTaskGroupException if the task group of the task assignment is not an SQL task group
     */
    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException, NotAValidTaskGroupException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) &&
            !newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString()))
            return;

        if (StringUtils.isBlank(newTaskAssignmentDTO.getTaskGroupId()) ||
            StringUtils.isBlank(newTaskAssignmentDTO.getSqlSolution())) {
            throw new MissingParameterException("Either the task group id or the solution is not set");
        }

        if(!taskGroupTypeFitsTaskType(newTaskAssignmentDTO)){
            throw new NotAValidTaskGroupException();
        }

        // Create task
        var optId = this.handleTaskCreation(newTaskAssignmentDTO);

        // Set dispatcher id of task
        newTaskAssignmentDTO.setTaskIdForDispatcher(
            optId.map(String::valueOf)
            .orElseThrow(() -> new DispatcherRequestFailedException("Dispatcher returned invalid id")));
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
            sqlClient.updateSQLExerciseSolution(id, solution);
        }else{
            throw new MissingParameterException("SqlSolution is missing");
        }
    }

    /**
     * Deletes an SQL task by sending a request to the dispatcher.
     *
     * @param taskAssignmentDTO the task assignment to be deleted
     */
    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO)  {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            sqlClient.deleteSQLExercise(id);
        }catch(NumberFormatException | DispatcherRequestFailedException ignore){
            // we ignore it if deletion is not successful
        }

    }

    // Private reqion

    /**
     * Creates or updates an SQL task group by sending a request to the dispatcher.
     * Sets the received dispatcher id for the task-group.
     * Updates the description of the task-group to contain schema-info and links to view the tables.
     * @param newTaskGroupDTO the task group to be created or updated
     * @param isNew true if the task group is new, false if it already exists
     * @throws TaskTypeSpecificOperationFailedException if an error occurs during the creation or update
     */
    private void createOrUpdateTaskGroup(NewTaskGroupDTO newTaskGroupDTO, boolean isNew) throws TaskTypeSpecificOperationFailedException {
        SqlDataDefinitionDTO body = constructTaskGroupDto(newTaskGroupDTO);

        SQLSchemaInfoDTO schemaInfo = proxyTaskGroupRequestToDispatcher(body);
        newTaskGroupDTO.setDispatcherId(String.valueOf(schemaInfo.getDiagnoseConnectionId()));
        updateSQLTaskGroupDescriptionWithLinksAndSchemaInfo(schemaInfo, newTaskGroupDTO, isNew);
    }

    /**
     * Updates the description of an SQL task group by appending links
     * to the specified tables and information about the schema of the tables
     *
     * @param newTaskGroupDTO the {@link TaskGroupDTO}
     */
    private void updateSQLTaskGroupDescriptionWithLinksAndSchemaInfo(SQLSchemaInfoDTO info, NewTaskGroupDTO newTaskGroupDTO, boolean isNew) {
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

        // Set updated description
        newTaskGroupDTO.setDescription(description);
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
        var response = sqlClient.createSQLExercise(solution, taskGroup);

        // Return dispatcher-id of the exercise
        return Optional.of(response);
    }

    /**
     * Constructs the DTO for the request to the dispatcher
     * @param newTaskGroupDTO the task group to be created
     * @return the DTO
     * @throws MissingParameterException if neither the insert statements for submission nor the insert statements for diagnose are set
     */
    private SqlDataDefinitionDTO constructTaskGroupDto(NewTaskGroupDTO newTaskGroupDTO) throws MissingParameterException {
        // Check if both insert statements are blank (if only one is blank, the dispatcher will use the other)
        if (StringUtils.isBlank(newTaskGroupDTO.getSqlInsertStatementsSubmission())
            && StringUtils.isBlank(newTaskGroupDTO.getSqlInsertStatementsDiagnose())) {
            throw new MissingParameterException("Either the insert statements for submission or the insert statements for diagnose must be set");
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
        return body;
    }

    /**
     * Proxies the request to the dispatcher
     * @param body the body of the request
     * @return the response
     * @throws TaskTypeSpecificOperationFailedException if an error occurs during the request
     */
    private SQLSchemaInfoDTO proxyTaskGroupRequestToDispatcher(SqlDataDefinitionDTO body) throws TaskTypeSpecificOperationFailedException {
        return sqlClient.executeDDLForSQL(body);
    }

    /**
     * Checks if the task group type fits the task type
     * @param newTaskAssignmentDTO the task assignment
     * @return true if the task group type fits the task type, false otherwise
     */
    private boolean taskGroupTypeFitsTaskType(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        // Fetch group to compare type
        var group = assignmentSPARQLEndpointService.getTaskGroupByName(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#") + 1));
        return group.filter(g -> g.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())).isPresent();
    }
}
