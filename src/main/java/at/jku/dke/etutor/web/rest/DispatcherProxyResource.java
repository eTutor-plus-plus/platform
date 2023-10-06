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
 * Contains actual REST endpoints that are exposed to the client and proxy request to dispatcher(s).
 * Aggregated for different task-types that are managed by the dispatcher.
 * Task-type specific proxy-functionality that is located in the respective service.
 */
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherProxyResource {
    private final DkeSubmissionClient dkeSubmissionClient;
    private final SqlClient sqlCLient;
    private final DatalogClient datalogClient;
    private final XQueryClient xQueryClient;
    public DispatcherProxyResource(DkeSubmissionClient dkeSubmissionClient,
                                   SqlClient sqlCLient,
                                   DatalogClient datalogClient,
                                   XQueryClient xQueryClient){
        this.dkeSubmissionClient = dkeSubmissionClient;
        this.sqlCLient = sqlCLient;
        this.datalogClient = datalogClient;
        this.xQueryClient = xQueryClient;
    }

    /**
     * Actual REST endpoints.
     * May be called by client, but also by other services.
     */
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


    @GetMapping(value="/sql/table/{tableName}")
    public ResponseEntity<String> getHTMLTableForSQL(@PathVariable String tableName, @RequestParam(defaultValue = "-1") int connId, @RequestParam(defaultValue="-1") int exerciseId, @RequestParam(defaultValue = "") String taskGroup) throws DispatcherRequestFailedException {
        return sqlCLient.getHTMLTableForSQL(tableName, connId, exerciseId, taskGroup);
    }

    /**
     * Requests the facts for a datalog task group in HTML format
     * @param id the id of the facts
     * @return the facts
     */
    @GetMapping("/datalog/facts/id/{id}")
    public ResponseEntity<String> getDLGFacts(@PathVariable int id) throws DispatcherRequestFailedException {
        return datalogClient.getDLGFacts(id);
    }

    /**
     * Requests the facts for a datalog task group as raw string
     * @param id the id of the facts
     * @return the facts
     */
    @GetMapping("/datalog/facts/id/{id}/asinputstream")
    public ResponseEntity<Resource> getDLGFactsAsInputStream(@PathVariable int id) throws DispatcherRequestFailedException {
        return datalogClient.getDLGFactsAsInputStream(id);
    }

    /**
     * Returns the xml  for an xquery taskgroup
     * @param id the file id of the xml
     * @return a ResponseEntity
     */
    @GetMapping("/xquery/xml/fileid/{id}")
    public ResponseEntity<String> getXMLForXQByFileId(@PathVariable int id) throws DispatcherRequestFailedException {
        return xQueryClient.getXMLForXQByFileId(id);
    }

    /**
     * Returns the xml  for an xquery taskgroup as inputstream
     * @param id the file id of the xml
     * @return a ResponseEntity containing the InputStreamResource
     */
    @GetMapping("/xquery/xml/fileid/{id}/asinputstream")
    public ResponseEntity<Resource> getXMLForXQByFileIdAsInputStream(@PathVariable int id) throws DispatcherRequestFailedException {
        return xQueryClient.getXMLForXQByFileIdAsInputStream(id);
    }
}
