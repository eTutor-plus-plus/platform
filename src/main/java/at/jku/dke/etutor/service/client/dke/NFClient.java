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

@Service
public non-sealed class NFClient extends AbstractDispatcherClient {

    private static final String BASE_URL = "/nf/exercise";

    protected NFClient(ApplicationProperties properties) {
        super(properties);
    }

    public Integer createExercise(NFExerciseDTO exerciseDTO) throws DispatcherRequestFailedException {
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

    public void deleteExercise(int id) throws DispatcherRequestFailedException {
        HttpRequest deleteRequest = getDeleteRequest(BASE_URL + "/" + id);
        String response = Objects.requireNonNull(sendRequest(deleteRequest, stringHandler).getBody());
        if(!Boolean.parseBoolean(response)) {
            throw new DispatcherRequestFailedException("Could not delete NF exercise.");
        }
    }

    public String getAssignmentText(int id) throws DispatcherRequestFailedException {
        HttpRequest getRequest = getGetRequest(BASE_URL + "/" + id + "/instruction");
        return Objects.requireNonNull(sendRequest(getRequest, stringHandler, 200).getBody());
    }
}
