package at.jku.dke.etutor.service.client.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.nf.NFExerciseDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.Objects;

/**
 * Client for communicating with the NF endpoint of the dispatcher
 */
@Service
public non-sealed class NFClient extends AbstractDispatcherClient {

    private static final String BASE_URL = "/nf/exercise";

    protected NFClient(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Requests the dispatcher to create a new exercise in the database from the supplied <code>NFExerciseDTO</code>.
     * @param exerciseDTO The <code>NFExerciseDTO</code> with the content of the new exercise
     * @return The id of the newly created exercise, -1 if an error occurs (passed through from the dispatcher)
     * @throws DispatcherRequestFailedException If the dispatcher fails to fulfill the request
     */
    public int createExercise(NFExerciseDTO exerciseDTO) throws DispatcherRequestFailedException {
        // source: https://stackoverflow.com/a/15786175 (Gerald Wimmer, 2024-01-05)
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            HttpRequest putRequest = getPutRequestWithBody(BASE_URL, objectWriter.writeValueAsString(exerciseDTO));
            ResponseEntity<String> response = Objects.requireNonNull(sendRequest(putRequest, stringHandler));
            String responseBody = Objects.requireNonNull(response.getBody());
            return Integer.parseInt(responseBody);
        } catch (JsonProcessingException j) {
            throw new DispatcherRequestFailedException("Could not serialize NF exercise because : " + j);
        }
    }

    /**
     * Requests the dispatcher to replace the specified exercise in the database with one specified in the supplied
     * <code>NFExerciseDTO</code>.
     * @param id The id of the exercise to be replaced
     * @param exerciseDTO The <code>NFExerciseDTO</code> whose content is to replace the existing exercise
     * @throws DispatcherRequestFailedException If the dispatcher fails to fulfill the request
     */
    public void modifyExercise(int id, NFExerciseDTO exerciseDTO) throws DispatcherRequestFailedException {
        // source: https://stackoverflow.com/a/15786175 (Gerald Wimmer, 2024-01-05)
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            HttpRequest postRequest = getPostRequestWithBody(BASE_URL + "/" + id, objectWriter.writeValueAsString(exerciseDTO)).build();
            String response = Objects.requireNonNull(sendRequest(postRequest, stringHandler).getBody());
            if(!Boolean.parseBoolean(response)) {
                throw new DispatcherRequestFailedException("Could not modify NF exercise.");
            }
        } catch (JsonProcessingException j) {
            throw new DispatcherRequestFailedException("Could not serialize NF exercise because : " + j);
        }
    }

    /**
     * Requests the dispatcher to delete the exercise with the specified id from the database.
     * @param id The id of the exercise to be deleted
     * @throws DispatcherRequestFailedException If the dispatcher fails to fulfill the request
     */
    public void deleteExercise(int id) throws DispatcherRequestFailedException {
        HttpRequest deleteRequest = getDeleteRequest(BASE_URL + "/" + id);
        String response = Objects.requireNonNull(sendRequest(deleteRequest, stringHandler).getBody());
        if(!Boolean.parseBoolean(response)) {
            throw new DispatcherRequestFailedException("Could not delete NF exercise.");
        }
    }

    /**
     * Requests the auto-generated assignment text for the exercise with the specified ID from the dispatcher.
     * @param id The id of the exercise whose assignment text is to be auto-generated
     * @return The auto-generated assignment text for the exercise with the specified ID from the dispatcher
     * @throws DispatcherRequestFailedException If the dispatcher fails to fulfill the request
     */
    public String getAssignmentText(int id) throws DispatcherRequestFailedException {
        HttpRequest getRequest = getGetRequest(BASE_URL + "/" + id + "/instruction");
        return Objects.requireNonNull(sendRequest(getRequest, stringHandler, 200).getBody());
    }
}
