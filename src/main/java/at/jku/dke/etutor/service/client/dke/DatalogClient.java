package at.jku.dke.etutor.service.client.dke;

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
public final class DatalogClient extends AbstractDispatcherClient {
    public DatalogClient(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Proxies the request to create a datalog task group to the dispatcher
     * @param groupDTO the {@link DatalogTaskGroupDTO} containing the name and the facts
     * @return the id of the newly created taskgroup
     */
    public Integer createDLGTaskGroup(DatalogTaskGroupDTO groupDTO) throws DispatcherRequestFailedException {
        String path = "/datalog/taskgroup";
        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(path, serialize(groupDTO)).build();
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not serialize the task group.");
        }
        var response = getResponseEntity(request, stringHandler);

        if(response.getBody() == null)
            throw new DispatcherRequestFailedException("No id has been returned by the dispatcher.");

        return Integer.parseInt(response.getBody());
    }

    /**
     * Proxies the request to update a datalog task group to the dispatcher
     * @param id the dispatcher id of the task group
     * @param newFacts the new facts to be updated
     */
    public void updateDLGTaskGroup(String id, String newFacts) throws DispatcherRequestFailedException {
        String path = "/datalog/taskgroup/"+id;
        var request = getPostRequestWithBody(path, newFacts).build();
        getResponseEntity(request, HttpResponse.BodyHandlers.discarding(), 200);
    }

    /**
     * Requests the deletion of a datalog task group from the dispatcher
     * @param id the id of the group
     */
    public void deleteDLGTaskGroup(int id) throws DispatcherRequestFailedException {
        String path = "/datalog/taskgroup/"+id;

        var request = getDeleteRequest(path);
        getResponseEntity(request, HttpResponse.BodyHandlers.discarding(), 200);
    }

    /**
     * Requests the creation of a datalog exercise
     * @param exerciseDTO the {@link DatalogExerciseDTO} wrapping the exercise information
     * @return the id of the newly created exercise
     */
    public Integer createDLGExercise(DatalogExerciseDTO exerciseDTO) throws DispatcherRequestFailedException {
        String path = "/datalog/exercise";

        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(path, serialize(exerciseDTO)).build();
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not serialize the exercise.");
        }
        var response = getResponseEntity(request, stringHandler,200);

        if (response.getBody() == null)
            throw new DispatcherRequestFailedException("No id has been returned by the dispatcher.");

        return Integer.parseInt(response.getBody());
    }

    /**
     * Requests modification of a datalog exercise
     * @param exerciseDTO the {@link DatalogExerciseDTO} with the new attributes
     * @param id the id of the exercise
     */
    public void modifyDLGExercise(DatalogExerciseDTO exerciseDTO,  int id) throws DispatcherRequestFailedException {
        String path = "/datalog/exercise/"+id;

        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(path, serialize(exerciseDTO)).build();
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not serialize the exercise.");
        }
        getResponseEntity(request, HttpResponse.BodyHandlers.discarding(), 200);
    }

    /**
     * Deletes resources associated with a given datalog exercise in the dispatcher
     * @param id the id of the datalog exercise
     */
    public void deleteDLGExercise(int id) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/datalog/exercise/" + id);
        getResponseEntity(request, HttpResponse.BodyHandlers.discarding(), 200);
    }

    // methods called by controller return response entities

    public ResponseEntity<String> getDLGFacts(int id) throws DispatcherRequestFailedException {
        var request = getGetRequest("/datalog/taskgroup/"+id);
        return getResponseEntity(request, stringHandler, 200);
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
