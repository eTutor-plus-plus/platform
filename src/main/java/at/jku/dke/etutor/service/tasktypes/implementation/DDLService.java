package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.client.dke.DDLClient;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.*;

/**
 * Service that handles all SQL DDL task-type specific operations.
 */
@Service
public class DDLService implements TaskTypeService {
    //region Fields
    private final DDLClient ddlClient;
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final ApplicationProperties properties;
    //endregion

    public DDLService(DDLClient ddlClient, AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, ApplicationProperties properties) {
        this.ddlClient = ddlClient;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.properties = properties;
    }

    /**
     * Funciton to create a SQL DDL task by sending a request to the dispatcher
     * @param newTaskAssignmentDTO Specifies the task assignment
     * @throws TaskTypeSpecificOperationFailedException
     * @throws NotAValidTaskGroupException
     */
    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException, NotAValidTaskGroupException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.DDLTask.toString())) {
            return;
        }

        if (StringUtils.isBlank(newTaskAssignmentDTO.getDdlSolution())) {
            throw new MissingParameterException("The solution is not set");
        }

        // Create task
        var optId = this.handleTaskCreation(newTaskAssignmentDTO);

        // Set the dispatcher id of the task
        newTaskAssignmentDTO.setTaskIdForDispatcher(
            optId.map(String::valueOf)
                .orElseThrow(() -> new DispatcherRequestFailedException("Dispatcher returned invalid id")));
    }

    /**
     * Function to update a SQL DDL task by sending a request to the dispatcher
     * @param taskAssignmentDTO Specifies the assignment to be updated
     * @throws TaskTypeSpecificOperationFailedException
     */
    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        if(StringUtils.isNotBlank(taskAssignmentDTO.getDdlSolution())){
            String solution = taskAssignmentDTO.getDdlSolution();
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            String insertStatements = taskAssignmentDTO.getDdlInsertStatements();
            String maxPoints = taskAssignmentDTO.getMaxPoints();
            String tablePoints = taskAssignmentDTO.getTablePoints();
            String columnPoints = taskAssignmentDTO.getColumnPoints();
            String primaryKeyPoints = taskAssignmentDTO.getPrimaryKeyPoints();
            String foreignKeyPoints = taskAssignmentDTO.getForeignKeyPoints();
            String constraintPoints = taskAssignmentDTO.getConstraintPoints();

            ddlClient.updateDDLExercise(id, solution, insertStatements, maxPoints, tablePoints, columnPoints, primaryKeyPoints, foreignKeyPoints, constraintPoints);
        }else{
            throw new MissingParameterException("DDLSolution is missing");
        }
    }

    /**
     * Delete a SQL DDL task by sending a request to the dispatcher
     * @param taskAssignmentDTO Specifies the task assignment
     * @throws TaskTypeSpecificOperationFailedException
     */
    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            ddlClient.deleteDDLExercise(id);
        }catch(NumberFormatException | DispatcherRequestFailedException ignore){
            // we ignore it if deletion is not successful
        }
    }

    //region Private methods

    /**
     * Creates an SQL exercise
     *
     * @param newTaskAssignmentDTO the new task assignment
     * @return the id of the created exercise
     */
    private Optional<Integer> handleTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        Objects.requireNonNull(newTaskAssignmentDTO.getDdlSolution());

        // Get solution required by the dispatcher to create the task
        String solution = newTaskAssignmentDTO.getDdlSolution();
        String insertStatements = newTaskAssignmentDTO.getDdlInsertStatements();
        String maxPoints = newTaskAssignmentDTO.getMaxPoints();
        String tablePoints = newTaskAssignmentDTO.getTablePoints();
        String columnPoints = newTaskAssignmentDTO.getColumnPoints();
        String primaryKeyPoints = newTaskAssignmentDTO.getPrimaryKeyPoints();
        String foreignKeyPoints = newTaskAssignmentDTO.getForeignKeyPoints();
        String constraintPoints = newTaskAssignmentDTO.getConstraintPoints();

        // Proxy request to dispatcher
        var response = ddlClient.createDDLExercise(solution, insertStatements, maxPoints, tablePoints, columnPoints, primaryKeyPoints, foreignKeyPoints, constraintPoints);

        // Return dispatcher-id of the exercise
        return Optional.of(response);
    }
    //endregion
}
