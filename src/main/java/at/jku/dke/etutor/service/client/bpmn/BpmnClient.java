package at.jku.dke.etutor.service.client.bpmn;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// NOTE: Do not at this client for an best-practice implementation of a client.
@Service
public non-sealed class BpmnClient extends AbstractBpmnDispatcherClient {
    public BpmnClient(ApplicationProperties properties) {
        super(properties);
    }

    public ResponseEntity<String> deleteBpmnExercise(int id) throws DispatcherRequestFailedException {
        String path = "/bpmn/exercise/id/"+id;
        var request = getDeleteRequest(path);

        return getResponseEntity(request, stringHandler);
    }

    public ResponseEntity<Integer> createBpmnExercise(String bpmnExercise) throws DispatcherRequestFailedException {
        String path = "/bpmn/exercise";
        HttpRequest request = null;
        request = getPostRequestWithBody(path, bpmnExercise).build();

        var response = getResponseEntity(request, stringHandler);

        if (response.getBody() == null) throw new DispatcherRequestFailedException("No id returned");

        int id = Integer.parseInt(response.getBody());
        return ResponseEntity.status(response.getStatusCodeValue()).body(id);
    }

    public ResponseEntity<Void> modifyBpmnExercise(String exercise, int id) throws DispatcherRequestFailedException {
        String path = "/bpmn/exercise/id/"+id;

        HttpRequest request = null;
        request = getPostRequestWithBody(path, exercise).build();
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }
}
