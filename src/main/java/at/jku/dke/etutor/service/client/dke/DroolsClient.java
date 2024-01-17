package at.jku.dke.etutor.service.client.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.drools.DroolsTaskDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.Objects;

/**
 * Client for interacting with the drools endpoint of the dispatcher.
 */
@Service
public non-sealed class DroolsClient extends AbstractDispatcherClient {
    public DroolsClient(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Sends the PUT-request for creating a Drools-exercise to the dispatcher
     * @param solution the solution for the exercise
     * @return a ResponseEntity
     */
    public Integer createDroolsExercise(String solution, int maxPoints, String classes, String objects,
                                        int errorWeighting, String validationClassname)
        throws DispatcherRequestFailedException {
        var exerciseDTO = new DroolsTaskDTO(solution, maxPoints, classes, objects, errorWeighting, validationClassname);
        HttpRequest request = null;
        try {
            request = getPostRequestWithBody("/drools/task/addTask", serialize(exerciseDTO)).build();
            return Integer.parseInt(Objects.requireNonNull(sendRequest(request, stringHandler, 200).getBody()));
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not serialize the exercise");
        }
    }

    /**
     * Sends the request to delete a task to the dispatcher
     *
     * @param id the task-id
     */
    public void deleteDroolsExercise(int id) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/drools/task/deleteTask/"+id);
        sendRequest(request, stringHandler, 200);
    }

    public void updateDroolsExercise(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        String id = taskAssignmentDTO.getTaskIdForDispatcher();
        String solution = taskAssignmentDTO.getDroolsSolution();
        int maxPoints = Integer.parseInt(taskAssignmentDTO.getMaxPoints());
        String classes = taskAssignmentDTO.getDroolsClasses();
        String objects = taskAssignmentDTO.getDroolsObjects();
        int errorWeighting = taskAssignmentDTO.getDroolsErrorWeighting();
        String validationClassname = taskAssignmentDTO.getDroolsValidationClassname();

        var droolsTaskDTO = new DroolsTaskDTO(solution, maxPoints, classes, objects, errorWeighting, validationClassname);
        HttpRequest request = null;
        try{
            request = getPutRequestWithBody("/drools/task/updateTask/"+id, serialize(droolsTaskDTO));
            sendRequest(request, stringHandler, 200);
        }catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not serialize the exercise");
        }

    }

}
