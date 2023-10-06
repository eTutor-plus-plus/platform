package at.jku.dke.etutor.service.client.bpmn;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public final class BpmnSubmissionClient extends AbstractBpmnDispatcherClient {
    public BpmnSubmissionClient(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Sends the submission to the dispatcher and returns the submission-id
     * @param submissionDto the submission
     * @return the submission-id
     */
    public ResponseEntity<String> postBpmnSubmission(String submissionDto, String language) throws DispatcherRequestFailedException {
        var request = getPostRequestWithBody("/submission", submissionDto)
            .setHeader(HttpHeaders.ACCEPT_LANGUAGE, language)
            .build();

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Requests a grading from the dispatcher
     * @param submissionId the submission-id identifying the grading
     * @return the response from the Bpmn Dispatcher
     */
    public ResponseEntity<String> getBpmnGrading(String submissionId) throws DispatcherRequestFailedException {
        var request = getGetRequest("/grading/"+submissionId);

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the submission UUID to the dispatcher and returns the Bpmn submission
     * @param submissionUUID the UUID identifying the Bpmn submission
     * @return the submission
     */
    public ResponseEntity<String> getBpmnSubmission(String submissionUUID) throws DispatcherRequestFailedException {
        var request = getGetRequest("/submission/"+submissionUUID);

        return getResponseEntity(request, stringHandler);
    }
}
