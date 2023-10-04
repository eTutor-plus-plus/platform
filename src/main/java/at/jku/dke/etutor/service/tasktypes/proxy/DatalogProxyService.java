package at.jku.dke.etutor.service.tasktypes.proxy;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogExerciseDTO;
import at.jku.dke.etutor.objects.dispatcher.dlg.DatalogTaskGroupDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DatalogProxyService extends DispatcherProxyService{
    public DatalogProxyService(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Proxies the request to create a datalog task group to the dispatcher
     * @param groupDTO the {@link DatalogTaskGroupDTO} containing the name and the facts
     * @return a {@link ResponseEntity} wrapping the id of the newly created task group
     */
    public ResponseEntity<Integer> createDLGTaskGroup(String groupDTO) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/datalog/taskgroup";
        var request = getPostRequestWithBody(url, groupDTO).build();
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
        String url = dispatcherURL+"/datalog/taskgroup/"+id;
        var request = getPostRequestWithBody(url, newFacts).build();
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Requests the deletion of a datalog task group from the dispatcher
     * @param id the id of the group
     * @return a {@link ResponseEntity} indicating if deletion has been successful
     */
    public ResponseEntity<Void> deleteDLGTaskGroup(int id) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/datalog/taskgroup/"+id;

        var request = getDeleteRequest(url);
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Requests the creation of a datalog exercise
     * @param exerciseDTO the {@link DatalogExerciseDTO} wrapping the exercise information
     * @return an {@link ResponseEntity} wrapping the assigned exercise id
     */
    public ResponseEntity<Integer> createDLGExercise(DatalogExerciseDTO exerciseDTO) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/datalog/exercise";

        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(url, new ObjectMapper().writeValueAsString(exerciseDTO)).build();
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
        String url = dispatcherURL+"/datalog/exercise/"+id;

        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(url, new ObjectMapper().writeValueAsString(exerciseDTO)).build();
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
        var request = getDeleteRequest(dispatcherURL + "/datalog/exercise/" + id);
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }
}
