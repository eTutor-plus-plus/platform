package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.client.dke.RTClient;
import at.jku.dke.etutor.service.client.dke.SqlClient;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import org.springframework.stereotype.Service;

@Service
public class RTService implements TaskTypeService {
    private final RTClient rtClient;
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final ApplicationProperties properties;

    public RTService(RTClient rtClient, AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, ApplicationProperties properties){
        this.rtClient = rtClient;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.properties = properties;
    }

    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException, NotAValidTaskGroupException {
        Integer id = rtClient.createRTTask(newTaskAssignmentDTO.getRtSolution(), newTaskAssignmentDTO.getMaxPoints());
        newTaskAssignmentDTO.setTaskIdForDispatcher(id.toString());
    }

    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        rtClient.editRTTask(taskAssignmentDTO.getRtSolution(), taskAssignmentDTO.getMaxPoints(), taskAssignmentDTO.getTaskIdForDispatcher());
    }

    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        rtClient.deleteRTTask(taskAssignmentDTO.getTaskIdForDispatcher());
    }
}
