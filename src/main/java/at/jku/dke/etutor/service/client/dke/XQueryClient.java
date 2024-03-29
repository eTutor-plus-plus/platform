package at.jku.dke.etutor.service.client.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.xq.XMLDefinitionDTO;
import at.jku.dke.etutor.objects.dispatcher.xq.XQExerciseDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.Objects;

/**
 * Client for interacting with the xquery endpoint of the dispatcher.
 */
@Service
public non-sealed class XQueryClient extends AbstractDispatcherClient {

    public XQueryClient(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Sends the POST-request for adding XML-files for a task group to the dispatcher
     * @param taskGroup the UUID for the task group
     * @param dto the dto containing the xml's
     * @return the file id of the created xml file from the dispatcher for retrieving
     */
    public String addXMLForXQTaskGroup(String taskGroup, XMLDefinitionDTO dto) throws DispatcherRequestFailedException {
        String path = "/xquery/xml/taskGroup/"+encodeValue(taskGroup);
        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(path, serialize(dto)).build();
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException(e.getMessage());
        }
        return sendRequest(request, stringHandler, 200).getBody();
    }

    /**
     * Sends the DELETE-request for xml resources for a specific task group to the dispatcher
     *
     * @param taskGroup the UUID for the task group
     */
    public void deleteXMLofXQTaskGroup(String taskGroup) throws DispatcherRequestFailedException {
        String path = "/xquery/xml/taskGroup/"+encodeValue(taskGroup);
        var request = getDeleteRequest(path);
        sendRequest(request, stringHandler, 200);
    }

    /**
     * Creates an XQuery exercise
     * @param taskGroup the taskGroup to associate the exercise with
     * @param exercise the exercise
     * @return the id of the created exercise
     */
    public Integer createXQExercise(String taskGroup, XQExerciseDTO exercise) throws DispatcherRequestFailedException {
        String path = "/xquery/exercise/taskGroup/"+encodeValue(taskGroup);
        HttpRequest.Builder request = null;
        ResponseEntity<String> response = null;
        try {
            request = getPostRequestWithBody(path, serialize(exercise));
            response = sendRequest(request.build(), stringHandler, 200);
            return Integer.parseInt(Objects.requireNonNull(response.getBody()));
        } catch (RuntimeException | IOException e) {
            throw new DispatcherRequestFailedException(e.getMessage());
        }
    }

    /**
     * Updates an xquery exercise
     * @param id the id of the exercise
     */
    public void updateXQExercise(int id, XQExerciseDTO dto) throws DispatcherRequestFailedException {
        String path = "/xquery/exercise/id/"+id;
        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(path, serialize(dto)).build();
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException(e.getMessage());
        }
        sendRequest(request, stringHandler, 200);
    }

    /**
     * methods called by controller return response entities
     */

    /**
     * Returns the solution query and the sortings XPath for an XQuery exercise
     * @param id the task id (dispatcher)
     * @return a ResponseEntity
     */
    public ResponseEntity<String> getXQExerciseInfo(int id) throws DispatcherRequestFailedException {
        var path = "/xquery/exercise/solution/id/"+id;
        var request = getGetRequest(path);
        return sendRequest(request, stringHandler, 200);
    }

    /**
     * Deletes an XQuery exercise
     *
     * @param id the exercise id
     */
    public void deleteXQExercise(int id) throws DispatcherRequestFailedException {
        String path = "/xquery/exercise/id/"+id;
        var request = getDeleteRequest(path);

        sendRequest(request, stringHandler, 200);
    }

    public ResponseEntity<String> getXMLForXQByFileId(int id) throws DispatcherRequestFailedException {
        String path = "/xquery/xml/fileid/"+id;
        var request = getGetRequest(path);

        return sendRequest(request, stringHandler, 200);
    }

    public ResponseEntity<Resource> getXMLForXQByFileIdAsInputStream(int id) throws DispatcherRequestFailedException {
        String xml = this.getXMLForXQByFileId(id).getBody();
        if(xml == null) throw new DispatcherRequestFailedException("XML cannot be null");

        ByteArrayInputStream ssInput = new ByteArrayInputStream(xml.getBytes());
        InputStreamResource fileInputStream = new InputStreamResource(ssInput);
        String fileName = id+".xml";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.set(HttpHeaders.CONTENT_TYPE, "text/xml");


        return new ResponseEntity<>(
            fileInputStream,
            headers,
            HttpStatus.OK
        );
    }
}
