package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.config.ApplicationProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Proxy that connects the frontend with the dispatcher
 */
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherProxyResource {
    private final String dispatcherURL;
    private final ApplicationProperties properties;

    public DispatcherProxyResource(ApplicationProperties properties){
        this.properties = properties;
        this.dispatcherURL = properties.getDispatcher().getUrl();
    }

    @GetMapping(value="/grading/{submissionId}")
    public ResponseEntity<String> getGrading(@PathVariable String submissionId){
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
            .uri(URI.create(dispatcherURL+"/grading/"+submissionId))
            .GET()
            .build();

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the submission to the dispatcher and returns the submission-id
     * @param submissionDto the submission
     * @return the submission-id
     */
    @PostMapping(value="/submission")
    public ResponseEntity<String> postSubmission(@RequestBody String submissionDto){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getPostRequestWithBody(dispatcherURL+"/submission", submissionDto);

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the DDL-Statements for creating an SQL-schema for an SQL-task-group to the dispatcher
     * @param ddl the statements
     * @return an response entity
     */
    @PostMapping(value="/sql/schema")
    public ResponseEntity<String> executeDDL(@RequestBody String ddl){
        var client = HttpClient.newHttpClient();
        var request = getPostRequestWithBody(dispatcherURL+"/sql/schema", ddl);

        return getStringResponseEntity(client, request);
    }

    /**
     * Utility method that sends an HttpRequest and returns the body wrapped inside an ResponseEntity<String>
     * @param client the HttpClient
     * @param request the HttpRequest
     * @return the ResponseEntity
     */
    @NotNull
    private ResponseEntity<String> getStringResponseEntity(HttpClient client, HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.ok(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
    }

    /**
     * Utility method that returns a Post-HttpRequest
     * @param url the url
     * @param json the body
     * @return the HttpRequest
     */
    @NotNull
    private HttpRequest getPostRequestWithBody(String url, String json){
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
    }
}
