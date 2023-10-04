package at.jku.dke.etutor.service.tasktypes.proxy;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class BpmnProxyService extends DispatcherProxyService{
    public BpmnProxyService(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Deletes an XQuery exercise
     * @param id the exercise id
     * @return a ResponseEntity
     */
    public ResponseEntity<String> deleteBpmnExercise(int id) throws DispatcherRequestFailedException {
        String url = bpmnDispatcherURL+"/bpmn/exercise/id/"+id;
        var request = getDeleteRequest(url);

        return getResponseEntity(request, stringHandler);
    }

    /**
     *
     * @param bpmnExercise
     * @return
     * @throws DispatcherRequestFailedException
     */
    public ResponseEntity<Integer> createBpmnExercise(String bpmnExercise) throws DispatcherRequestFailedException {
        String url = this.bpmnDispatcherURL +"/bpmn/exercise";
        HttpRequest request = null;
        request = getPostRequestWithBody(url, bpmnExercise).build();

        var response = getResponseEntity(request, stringHandler);

        if (response.getBody() == null) throw new DispatcherRequestFailedException("No id returned");

        int id = Integer.parseInt(response.getBody());
        return ResponseEntity.status(response.getStatusCodeValue()).body(id);
    }

    /**
     * Requests modification of a datalog exercise
     * @return a {@link ResponseEntity} indicating if the udpate has been successful
     */
    public ResponseEntity<Void> modifyBpmnExercise(String exercise, int id) throws DispatcherRequestFailedException {
        String url = bpmnDispatcherURL+"/bpmn/exercise/id/"+id;

        HttpRequest request = null;
        request = getPostRequestWithBody(url, exercise).build();
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }
}
