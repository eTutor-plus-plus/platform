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
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException, DispatcherRequestFailedException {
        Integer createTask = rtClient.createRTTask(newTaskAssignmentDTO.getRtSolution(), newTaskAssignmentDTO.getMaxPoints());
        newTaskAssignmentDTO.setTaskIdForDispatcher(createTask.toString());
    }

    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        int updateTask = rtClient.editRTTask(taskAssignmentDTO.getRtSolution(), taskAssignmentDTO.getMaxPoints(), taskAssignmentDTO.getTaskIdForDispatcher());
        if (updateTask == -1){
            throw new DispatcherRequestFailedException("Syntax Error: Cannot parse your solution to a Json-object!");
        }
        if (updateTask == -2){
            throw new DispatcherRequestFailedException("Syntax Error: The provided sample solution is incorrect!");
        }
        if (updateTask == -3){
            throw new DispatcherRequestFailedException("Semantik Error: The sum of the weighted points are higher than the total points");
        }
    }

    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        rtClient.deleteRTTask(taskAssignmentDTO.getTaskIdForDispatcher());
    }
}
