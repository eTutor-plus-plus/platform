package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.tasktypes.proxy.DatalogProxyService;
import at.jku.dke.etutor.service.tasktypes.proxy.DkeSubmissionProxyService;
import at.jku.dke.etutor.service.tasktypes.proxy.SqlProxyService;
import at.jku.dke.etutor.service.tasktypes.proxy.XQueryProxyService;
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
    private final DkeSubmissionProxyService dispatcherSubmissionProxyService;
    private final SqlProxyService sqlProxyService;
    private final DatalogProxyService datalogProxyService;
    private final XQueryProxyService xQueryProxyService;
    public DispatcherProxyResource(DkeSubmissionProxyService dispatcherSubmissionProxyService,
                                   SqlProxyService sqlProxyService,
                                   DatalogProxyService datalogProxyService,
                                   XQueryProxyService xQueryProxyService){
        this.dispatcherSubmissionProxyService = dispatcherSubmissionProxyService;
        this.sqlProxyService = sqlProxyService;
        this.datalogProxyService = datalogProxyService;
        this.xQueryProxyService = xQueryProxyService;
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
        return dispatcherSubmissionProxyService.getGrading(submissionId);

    }

    /**
     * Sends the submission to the dispatcher and returns the submission-id
     * @param submissionDto the submission
     * @return the submission-id
     */
    @PostMapping(value="/submission")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> postSubmission(@RequestBody String submissionDto, @RequestHeader("Accept-Language") String language) throws DispatcherRequestFailedException {
        return dispatcherSubmissionProxyService.postSubmission(submissionDto, language);
    }

    /**
     * Sends the submission UUID to the dispatcher and returns the submission
     * @param submissionUUID the UUID identifying the submission
     * @return the submission
     */
    @GetMapping(value="/submission/{submissionUUID}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getSubmission(@PathVariable String submissionUUID) throws DispatcherRequestFailedException {
        return dispatcherSubmissionProxyService.getSubmission(submissionUUID);
    }


    @GetMapping(value="/sql/table/{tableName}")
    public ResponseEntity<String> getHTMLTableForSQL(@PathVariable String tableName, @RequestParam(defaultValue = "-1") int connId, @RequestParam(defaultValue="-1") int exerciseId, @RequestParam(defaultValue = "") String taskGroup) throws DispatcherRequestFailedException {
        return sqlProxyService.getHTMLTableForSQL(tableName, connId, exerciseId, taskGroup);
    }

    /**
     * Requests the facts for a datalog task group in HTML format
     * @param id the id of the facts
     * @return the facts
     */
    @GetMapping("/datalog/facts/id/{id}")
    public ResponseEntity<String> getDLGFacts(@PathVariable int id) throws DispatcherRequestFailedException {
        return datalogProxyService.getDLGFacts(id);
    }

    /**
     * Requests the facts for a datalog task group as raw string
     * @param id the id of the facts
     * @return the facts
     */
    @GetMapping("/datalog/facts/id/{id}/asinputstream")
    public ResponseEntity<Resource> getDLGFactsAsInputStream(@PathVariable int id) throws DispatcherRequestFailedException {
        return datalogProxyService.getDLGFactsAsInputStream(id);
    }

    /**
     * Returns the xml  for an xquery taskgroup
     * @param id the file id of the xml
     * @return a ResponseEntity
     */
    @GetMapping("/xquery/xml/fileid/{id}")
    public ResponseEntity<String> getXMLForXQByFileId(@PathVariable int id) throws DispatcherRequestFailedException {
        return xQueryProxyService.getXMLForXQByFileId(id);
    }

    /**
     * Returns the xml  for an xquery taskgroup as inputstream
     * @param id the file id of the xml
     * @return a ResponseEntity containing the InputStreamResource
     */
    @GetMapping("/xquery/xml/fileid/{id}/asinputstream")
    public ResponseEntity<Resource> getXMLForXQByFileIdAsInputStream(@PathVariable int id) throws DispatcherRequestFailedException {
        return xQueryProxyService.getXMLForXQByFileIdAsInputStream(id);
    }
}
