package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.client.dke.DatalogClient;
import at.jku.dke.etutor.service.client.dke.DkeSubmissionClient;
import at.jku.dke.etutor.service.client.dke.SqlClient;
import at.jku.dke.etutor.service.client.dke.XQueryClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contains actual REST endpoints that are exposed to the client and proxy requests
 * (submissions, gradings) to dispatcher(s).
 */
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherSubmissionResource {
    private final DkeSubmissionClient dkeSubmissionClient;
    public DispatcherSubmissionResource(DkeSubmissionClient dkeSubmissionClient){
        this.dkeSubmissionClient = dkeSubmissionClient;
    }
    /**
     * Requests a grading from the dispatcher
     * @param submissionId the submission-id identifying the grading
     * @return the response from the dispatcher
     */
    @GetMapping(value="/grading/{submissionId}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getGrading(@PathVariable String submissionId) throws DispatcherRequestFailedException {
        return dkeSubmissionClient.getGrading(submissionId);
    }

    /**
     * Sends the submission to the dispatcher and returns the submission-id
     * @param submissionDto the submission
     * @return the submission-id
     */
    @PostMapping(value="/submission")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> postSubmission(@RequestBody String submissionDto, @RequestHeader("Accept-Language") String language) throws DispatcherRequestFailedException {
        return dkeSubmissionClient.postSubmission(submissionDto, language);
    }

    /**
     * Sends the submission UUID to the dispatcher and returns the submission
     * @param submissionUUID the UUID identifying the submission
     * @return the submission
     */
    @GetMapping(value="/submission/{submissionUUID}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getSubmission(@PathVariable String submissionUUID) throws DispatcherRequestFailedException {
        return dkeSubmissionClient.getSubmission(submissionUUID);
    }
}
