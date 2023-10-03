package at.jku.dke.etutor.service.tasktypes.proxy;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

/**
 * Contains actual REST endpoints that are exposed to the client.
 * Methods may be called from client but also from other services.
 * Aggregated for different task-types that are managed by the dispatcher.
 * Task-type specific proxy-functionality that is only consumed by the server is located in the respective service.
 */
@Configuration
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherSubmissionProxyResource extends DispatcherProxyService {
    public DispatcherSubmissionProxyResource(ApplicationProperties properties) {
        super(properties);
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
        var request = getGetRequest(dispatcherURL+"/grading/"+submissionId);

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Requests a grading from the dispatcher
     * @param submissionId the submission-id identifying the grading
     * @return the response from the Bpmn Dispatcher
     */
    @GetMapping(value="/grading/bpmn/{submissionId}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getBpmnGrading(@PathVariable String submissionId) throws DispatcherRequestFailedException {
        var request = getGetRequest(bpmnDispatcherURL+"/grading/"+submissionId);

        return getResponseEntity(request, stringHandler);
    }


    /**
     * Sends the submission to the dispatcher and returns the submission-id
     * @param submissionDto the submission
     * @return the submission-id
     */
    @PostMapping(value="/submission")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> postSubmission(@RequestBody String submissionDto, @RequestHeader("Accept-Language") String language) throws DispatcherRequestFailedException {
        var request = getPostRequestWithBody(dispatcherURL+"/submission", submissionDto)
            .setHeader(HttpHeaders.ACCEPT_LANGUAGE, language)
            .build();

        return getResponseEntity(request, stringHandler);
    }
    /**
     * Sends the submission to the dispatcher and returns the submission-id
     * @param submissionDto the submission
     * @return the submission-id
     */
    @PostMapping(value="/bpmn/submission")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> postBpmnSubmission(@RequestBody String submissionDto, @RequestHeader("Accept-Language") String language) throws DispatcherRequestFailedException {
        var request = getPostRequestWithBody(bpmnDispatcherURL+"/submission", submissionDto)
            .setHeader(HttpHeaders.ACCEPT_LANGUAGE, language)
            .build();

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the submission UUID to the dispatcher and returns the submission
     * @param submissionUUID the UUID identifying the submission
     * @return the submission
     */
    @GetMapping(value="/submission/{submissionUUID}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getSubmission(@PathVariable String submissionUUID) throws DispatcherRequestFailedException {
        var request = getGetRequest(dispatcherURL+"/submission/"+submissionUUID);

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the submission UUID to the dispatcher and returns the Bpmn submission
     * @param submissionUUID the UUID identifying the Bpmn submission
     * @return the submission
     */
    @GetMapping(value="/submission/bpmn/{submissionUUID}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getBpmnSubmission(@PathVariable String submissionUUID) throws DispatcherRequestFailedException {
        var request = getGetRequest(bpmnDispatcherURL+"/submission/"+submissionUUID);

        return getResponseEntity(request, stringHandler);
    }


    @GetMapping(value="/sql/table/{tableName}")
    public ResponseEntity<String> getHTMLTableForSQL(@PathVariable String tableName, @RequestParam(defaultValue = "-1") int connId, @RequestParam(defaultValue="-1") int exerciseId, @RequestParam(defaultValue = "") String taskGroup) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/sql/table/"+encodeValue(tableName);
        // Table names are only unique in the namespace of a task group, which can be identified in the dispatcher by the connection-id, the exercise-id, or the taskgroup-name
        if(connId != -1){
            url += "?connId="+connId;
        } else if(exerciseId != -1){
            url += "?exerciseId="+exerciseId;
        }else if(!taskGroup.equalsIgnoreCase("")){
            url+="?taskGroup="+taskGroup;
        }
        var request = getGetRequest(url);


        return getResponseEntity(request, stringHandler);
    }

    /**
     * Requests the facts for a datalog task group in HTML format
     * @param id the id of the facts
     * @return the facts
     */
    @GetMapping("/datalog/facts/id/{id}")
    public ResponseEntity<String> getDLGFacts(@PathVariable int id) throws DispatcherRequestFailedException {
        var request = getGetRequest(dispatcherURL+"/datalog/taskgroup/"+id);
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Requests the facts for a datalog task group as raw string
     * @param id the id of the facts
     * @return the facts
     */
    @GetMapping("/datalog/facts/id/{id}/asinputstream")
    public ResponseEntity<Resource> getDLGFactsAsInputStream(@PathVariable int id) throws DispatcherRequestFailedException {
        var request = getGetRequest(dispatcherURL+"/datalog/taskgroup/"+id+"/raw");
        var response = getResponseEntity(request, stringHandler);
        var facts = response.getBody();

        if(facts != null && response.getStatusCodeValue() == 200){
            ByteArrayInputStream ssInput = new ByteArrayInputStream(facts.getBytes());
            InputStreamResource fileInputStream = new InputStreamResource(ssInput);
            String fileName = id+".dlv";

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.set(HttpHeaders.CONTENT_TYPE, "text/xml");

            return new ResponseEntity<>(
                fileInputStream,
                headers,
                HttpStatus.OK
            );
        }
        return ResponseEntity.status(response.getStatusCodeValue()).body(new InputStreamResource(null));
    }

    /**
     * Returns the xml  for an xquery taskgroup
     * @param id the file id of the xml
     * @return a ResponseEntity
     */
    @GetMapping("/xquery/xml/fileid/{id}")
    public ResponseEntity<String> getXMLForXQByFileId(@PathVariable int id) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/xml/fileid/"+id;
        var request = getGetRequest(url);

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Returns the xml  for an xquery taskgroup as inputstream
     * @param id the file id of the xml
     * @return a ResponseEntity containing the InputStreamResource
     */
    @GetMapping("/xquery/xml/fileid/{id}/asinputstream")
    public ResponseEntity<Resource> getXMLForXQByFileIdAsInputStream(@PathVariable int id) throws DispatcherRequestFailedException {
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
