package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.client.dke.DDLClient;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import org.springframework.stereotype.Service;

/**
 * Service that handles all SQL DDL task-type specific operations.
 */
@Service
public class DDLService implements TaskTypeService {
    private final DDLClient ddlClient;
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final ApplicationProperties properties;

    public DDLService(DDLClient ddlClient, AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, ApplicationProperties properties) {
        this.ddlClient = ddlClient;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.properties = properties;
    }

    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException, NotAValidTaskGroupException {

    }

    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {

    }

    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {

    }

    //region Private methods

    //endregion
}
