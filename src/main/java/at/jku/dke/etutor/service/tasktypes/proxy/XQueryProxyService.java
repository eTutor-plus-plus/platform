package at.jku.dke.etutor.service.tasktypes.proxy;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;

@Service
public class XQueryProxyService extends DispatcherProxyService {

    public XQueryProxyService(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Sends the POST-request for adding XML-files for a task group to the dispatcher
     * @param taskGroup the UUID for the task group
     * @param dto the dto containing the xml's
     * @return the file id of the created xml file from the dispatcher for retrieving
     */
    public ResponseEntity<String> addXMLForXQTaskGroup(String taskGroup, String dto) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/xml/taskGroup/"+encodeValue(taskGroup);
        var request = getPostRequestWithBody(url, dto).build();
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the DELETE-request for xml resources for a specific task group to the dispatcher
     * @param taskGroup the UUID for the task group
     * @return the file id of the created xml file from the dispatcher for retrieving
     */
    public ResponseEntity<String> deleteXMLofXQTaskGroup(String taskGroup) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/xml/taskGroup/"+encodeValue(taskGroup);
        var request = getDeleteRequest(url);
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Creates an XQuery exercise
     * @param taskGroup the taskGroup to associate the exercise with
     * @param exercise the exercise
     * @return a ResponseEntity
     */
    public ResponseEntity<Integer> createXQExercise(String taskGroup, String exercise) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/exercise/taskGroup/"+encodeValue(taskGroup);
        var request = getPostRequestWithBody(url, exercise);
        HttpResponse<String> response = null;
        try {
            response = client.send(request.build(), stringHandler);
            if(response.statusCode() == 500) throw new DispatcherRequestFailedException(response.body());
            return ResponseEntity.ok(Integer.parseInt(response.body()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(500).body(-1);
    }

    /**
     * Updates an xquery exercise
     * @param id the id of the exercise
     * @return a ResponseEntity
     */
    public ResponseEntity<String> updateXQExercise(int id, String dto) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/exercise/id/"+id;
        var request = getPostRequestWithBody(url, dto).build();

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Returns the solution query and the sortings XPath for an XQuery exercise
     * @param id the task id (dispatcher)
     * @return a ResponseEntity
     */
    public ResponseEntity<String> getXQExerciseInfo(int id) throws DispatcherRequestFailedException {
        var url = dispatcherURL + "/xquery/exercise/solution/id/"+id;
        var request = getGetRequest(url);
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Deletes an XQuery exercise
     * @param id the exercise id
     * @return a ResponseEntity
     */
    public ResponseEntity<String> deleteXQExercise(int id) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/exercise/id/"+id;
        var request = getDeleteRequest(url);

        return getResponseEntity(request, stringHandler);
    }
}
