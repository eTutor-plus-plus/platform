package at.jku.dke.etutor.service.tasktypes.proxy.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogExerciseDTO;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogTaskGroupDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public final class DatalogProxyService extends AbstractDispatcherProxyService {
    public DatalogProxyService(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Proxies the request to create a datalog task group to the dispatcher
     * @param groupDTO the {@link DatalogTaskGroupDTO} containing the name and the facts
     * @return a {@link ResponseEntity} wrapping the id of the newly created task group
     */
    public ResponseEntity<Integer> createDLGTaskGroup(String groupDTO) throws DispatcherRequestFailedException {
        String path = "/datalog/taskgroup";
        var request = getPostRequestWithBody(path, groupDTO).build();
        var response = getResponseEntity(request, stringHandler);

        if(response.getBody() == null) throw new DispatcherRequestFailedException("No id has been returned by the dispatcher.");

        var id = Integer.parseInt(response.getBody());
        return ResponseEntity.status(response.getStatusCodeValue()).body(id);
    }

    /**
     * Proxies the request to update a datalog task group to the dispatcher
     * @param id the dispatcher id of the task group
     * @param newFacts the new facts to be updated
     * @return an {@link ResponseEntity} indicating whether the update has been successful
     */
    public ResponseEntity<Void> updateDLGTaskGroup(String id, String newFacts) throws DispatcherRequestFailedException {
        String path = "/datalog/taskgroup/"+id;
        var request = getPostRequestWithBody(path, newFacts).build();
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Requests the deletion of a datalog task group from the dispatcher
     * @param id the id of the group
     * @return a {@link ResponseEntity} indicating if deletion has been successful
     */
    public ResponseEntity<Void> deleteDLGTaskGroup(int id) throws DispatcherRequestFailedException {
        String path = "/datalog/taskgroup/"+id;

        var request = getDeleteRequest(path);
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Requests the creation of a datalog exercise
     * @param exerciseDTO the {@link DatalogExerciseDTO} wrapping the exercise information
     * @return an {@link ResponseEntity} wrapping the assigned exercise id
     */
    public ResponseEntity<Integer> createDLGExercise(DatalogExerciseDTO exerciseDTO) throws DispatcherRequestFailedException {
        String path = "/datalog/exercise";

        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(path, new ObjectMapper().writeValueAsString(exerciseDTO)).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(-1);
        }
        var response = getResponseEntity(request, stringHandler);

        if (response.getBody() == null) throw new DispatcherRequestFailedException("No id has been returned by the dispatcher.");

        var id = Integer.parseInt(response.getBody());
        return ResponseEntity.status(response.getStatusCodeValue()).body(id);
    }

    /**
     * Requests modification of a datalog exercise
     * @param exerciseDTO the {@link DatalogExerciseDTO} with the new attributes
     * @param id the id of the exercise
     * @return a {@link ResponseEntity} indicating if the udpate has been successful
     */
    public ResponseEntity<Void> modifyDLGExercise(DatalogExerciseDTO exerciseDTO,  int id) throws DispatcherRequestFailedException {
        String path = "/datalog/exercise/"+id;

        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(path, new ObjectMapper().writeValueAsString(exerciseDTO)).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Deletes resources associated with a given datalog exercise in the dispatcher
     * @param id the id of the datalog exercise
     * @return a {@link ResponseEntity} indicating if deletion has been successful
     */
    public ResponseEntity<Void> deleteDLGExercise(int id) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/datalog/exercise/" + id);
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }

    public ResponseEntity<String> getDLGFacts(int id) throws DispatcherRequestFailedException {
        var request = getGetRequest("/datalog/taskgroup/"+id);
        return getResponseEntity(request, stringHandler);
    }

    public ResponseEntity<Resource> getDLGFactsAsInputStream(int id) throws DispatcherRequestFailedException {
        var request = getGetRequest("/datalog/taskgroup/"+id+"/raw");
        var response = getResponseEntity(request, stringHandler);
        var facts = response.getBody();

        if(facts != null && response.getStatusCodeValue() == 200){
            ByteArrayInputStream ssInput = new ByteArrayInputStream(facts.getBytes());
            InputStreamResource fileInputStream = new InputStreamResource(ssInput);
            String fileName = id+".dlv";

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.set(HttpHeaders.CONTENT_TYPE, "text/xml");

            return new ResponseEntity<>(
                fileInputStream,
                headers,
                HttpStatus.OK
            );
        }
        return ResponseEntity.status(response.getStatusCodeValue()).body(new InputStreamResource(null));
    }
}
