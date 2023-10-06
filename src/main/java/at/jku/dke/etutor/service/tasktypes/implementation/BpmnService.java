package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.client.bpmn.BpmnClient;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BpmnService implements TaskTypeService {
    private final BpmnClient bpmnClient;
    public BpmnService(BpmnClient bpmnClient) {
        this.bpmnClient = bpmnClient;
    }

    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException, NotAValidTaskGroupException {
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.BpmnTask.toString()))
            return;

        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getBpmnTestConfig())){

            // Create task
            int id = this.handleTaskCreation(newTaskAssignmentDTO);

            // Set the returned id of the task
            if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id + "");
        }else{ // Creation failed, either because no id and no group has been set or, in the case of the creation of a new task, not enough info has been provided
            throw new MissingParameterException("No BPMN test configuration has been provided");
        }

    }

    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        if (StringUtils.isBlank(taskAssignmentDTO.getBpmnTestConfig())) {
            throw new MissingParameterException("BpmnTestConfig is missing");
        }

        String exercise = taskAssignmentDTO.getBpmnTestConfig();
        bpmnClient.modifyBpmnExercise(exercise, Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()));
    }

    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            bpmnClient.deleteBpmnExercise(id);
        }catch (NumberFormatException e) {
            throw new DispatcherRequestFailedException("Dispatcher id is not a number");
        }
    }

    private int handleTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        String bpmnExercise = newTaskAssignmentDTO.getBpmnTestConfig();
        ResponseEntity<Integer> response = bpmnClient.createBpmnExercise(bpmnExercise);
        if(response.getBody() != null) return response.getBody();
        else return -1;
    }
}
