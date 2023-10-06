package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.client.bpmn.BpmnSubmissionClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bpmn/dispatcher")
public class BpmnDispatcherProxyResource {

    private final BpmnSubmissionClient bpmnSubmissionClient;

    public BpmnDispatcherProxyResource(BpmnSubmissionClient bpmnSubmissionClient) {
        this.bpmnSubmissionClient = bpmnSubmissionClient;
    }


    /**
     * Requests a grading from the dispatcher
     *
     * @param submissionId the submission-id identifying the grading
     * @return the response from the Bpmn Dispatcher
     */
    @GetMapping(value = "/grading/{submissionId}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getBpmnGrading(@PathVariable String submissionId) throws DispatcherRequestFailedException {
        return bpmnSubmissionClient.getBpmnGrading(submissionId);
    }

    /**
     * Sends the submission to the dispatcher and returns the submission-id
     *
     * @param submissionDto the submission
     * @return the submission-id
     */
    @PostMapping(value = "/submission")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> postBpmnSubmission(@RequestBody String submissionDto, @RequestHeader("Accept-Language") String language) throws DispatcherRequestFailedException {
        return bpmnSubmissionClient.postBpmnSubmission(submissionDto, language);
    }

    /**
     * Sends the submission UUID to the dispatcher and returns the Bpmn submission
     *
     * @param submissionUUID the UUID identifying the Bpmn submission
     * @return the submission
     */
    @GetMapping(value = "/submission/{submissionUUID}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getBpmnSubmission(@PathVariable String submissionUUID) throws DispatcherRequestFailedException {
        return bpmnSubmissionClient.getBpmnSubmission(submissionUUID);
    }

}
