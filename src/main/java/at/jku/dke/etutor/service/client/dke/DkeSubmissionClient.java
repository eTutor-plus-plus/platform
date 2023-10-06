package at.jku.dke.etutor.service.client.dke;
import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
/**
 * Client for interacting with the submission endpoint of the dispatcher.
 */
@Service
public non-sealed class DkeSubmissionClient extends AbstractDispatcherClient {

    public DkeSubmissionClient(ApplicationProperties properties) {
        super(properties);
    }

    public ResponseEntity<String> getGrading(String submissionId) throws DispatcherRequestFailedException {
        var request = getGetRequest("/grading/"+submissionId);

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the submission UUID to the dispatcher and returns the submission
     * @param submissionUUID the UUID identifying the submission
     * @return the submission
     */
    public ResponseEntity<String> getSubmission(String submissionUUID) throws DispatcherRequestFailedException {
        var request = getGetRequest("/submission/"+submissionUUID);

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the submission to the dispatcher and returns the submission-id
     * @param submissionDto the submission
     * @return the submission-id
     */
    public ResponseEntity<String> postSubmission(String submissionDto, String language) throws DispatcherRequestFailedException {
        var request = getPostRequestWithBody("/submission", submissionDto)
            .setHeader(HttpHeaders.ACCEPT_LANGUAGE, language)
            .build();

        return getResponseEntity(request, stringHandler);
    }
}
