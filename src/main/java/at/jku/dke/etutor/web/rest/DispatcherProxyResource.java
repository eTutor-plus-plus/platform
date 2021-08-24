package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.config.ApplicationProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Proxy that connects the frontend with the dispatcher
 */
@Configuration
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherProxyResource {
    private final String dispatcherURL;
    private final ApplicationProperties properties;

    public DispatcherProxyResource(ApplicationProperties properties){
        this.properties = properties;
        this.dispatcherURL = properties.getDispatcher().getUrl();
    }

    /**
     * Requests a grading from the dispatcher
     * @param submissionId the submission-id identifying the grading
     * @return the response from the dispatcher
     */
    @GetMapping(value="/grading/{submissionId}")
    public ResponseEntity<String> getGrading(@PathVariable String submissionId){
        var client = getHttpClient();
        var request = getGetRequest(dispatcherURL+"/grading/"+submissionId);

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the submission to the dispatcher and returns the submission-id
     * @param submissionDto the submission
     * @return the submission-id
     */
    @PostMapping(value="/submission")
    public ResponseEntity<String> postSubmission(@RequestBody String submissionDto, @RequestHeader("Accept-Language") String language){
        var client = getHttpClient();
        var request = getPostRequestWithBody(dispatcherURL+"/submission", submissionDto)
            .setHeader(HttpHeaders.ACCEPT_LANGUAGE, language)
            .build();

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the DDL-Statements for creating an SQL-schema for an SQL-task-group to the dispatcher
     * @param ddl the statements
     * @return an response entity
     */
    @PostMapping(value="/sql/schema")
    public ResponseEntity<String> executeDDL(@RequestBody String ddl){
        var client = getHttpClient();
        var request = getPostRequestWithBody(dispatcherURL+"/sql/schema", ddl).build();

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the PUT-request for creating an SQL-exercise to the dispatcher
     * @param solution the solution for the exercise
     * @param schemaName the schema/task-group
     * @param id the exercise-id
     * @return an ResponseEntity
     */
    @PutMapping(value="/sql/exercise/{schemaName}/{id}")
    public ResponseEntity<String> createExercise(@RequestBody String solution, @PathVariable String schemaName, @PathVariable int id){
        var client = getHttpClient();
        var request = getPutRequestWithBody(dispatcherURL+"/sql/exercise/"+schemaName+"/"+id, solution);

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the GET-request for getting the solution to an SQL-exercise to the dispatcher
     * @return a ResponseEntity
     */
    @GetMapping(value="/sql/exercise/{id}/solution")
    public ResponseEntity<String> getSolution(@PathVariable int id){
        var client = getHttpClient();
        var request = getGetRequest(dispatcherURL+"/sql/exercise/"+id+"/solution");

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the request to update the solution of an existing exercise to the dispatcher
     * @param id the id
     * @param newSolution the new solution
     * @return a ResponseEntity as received by the dispatcher
     */
    @PostMapping(value="/sql/exercise/{id}/solution")
    public ResponseEntity<String> updateExerciseSolution(@PathVariable int id, @RequestBody String newSolution){
        var client = getHttpClient();
        var request = getPostRequestWithBody(dispatcherURL+"/sql/exercise/"+id+"/solution", newSolution).build();

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the request to delete a schema to the dispatcher
     * @param schemaName the schema
     * @return a ResponseEntity
     */
    @DeleteMapping(value="/sql/schema/{schemaName}")
    public ResponseEntity<String> deleteSchema(@PathVariable String schemaName){
        var client = getHttpClient();
        var request = getDeleteRequest(dispatcherURL+"/sql/schema/"+schemaName);
        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the request to get an available exercise-id to the dispatcher
     * @return the id
     */
    @GetMapping(value="/sql/exercise/reservation")
    public ResponseEntity<String> getExerciseID(){
        var client = getHttpClient();
        var request = getGetRequest(dispatcherURL+"/sql/exercise/reservation");
        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the reuqest for deleting a connection associated with a given schema to the dispatcher
     * @param schemaName the schema
     * @return a ResponseEntity as received by the dispatcher
     */
    @DeleteMapping(value="/sql/schema/{schemaName}/connection")
    public ResponseEntity<String> deleteConnection(@PathVariable String schemaName){
        var client = getHttpClient();
        var request = getDeleteRequest(dispatcherURL+"/sql/schema/"+schemaName+"/connection");
        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the request to delete an exercise to the dispatcher
     * @param id the exercise-id
     * @return the response from the dispatcher
     */
    @DeleteMapping(value = "/sql/exercise/{id}")
    public ResponseEntity<String> deleteExercise(@PathVariable int id){
        var client = getHttpClient();
        var request = getDeleteRequest(dispatcherURL+"/sql/exercise/"+id);
        return getStringResponseEntity(client, request);
    }

    @GetMapping(value="sql/table/{tableName}")
    public ResponseEntity<String> getHTMLTable(@PathVariable String tableName, @RequestParam(defaultValue="-1") int exerciseId, @RequestParam(defaultValue = "") String taskGroup){
        String url = dispatcherURL+"/sql/table/"+tableName;
        if(exerciseId != -1){
            url += "?exerciseId="+exerciseId;
            if(!taskGroup.equalsIgnoreCase("")){
                url += "&&taskGroup="+taskGroup;
            }
        }else if(!taskGroup.equalsIgnoreCase("")){
            url+="?taskGroup="+taskGroup;
        }
        var client = getHttpClient();
        var request = getGetRequest(url);


        return getStringResponseEntity(client, request);
    }

    /**
     * Utility method that sends an HttpRequest and returns the response-body wrapped inside an ResponseEntity<String>
     * @param client the HttpClient
     * @param request the HttpRequest
     * @return the ResponseEntity
     */
    @NotNull
    private ResponseEntity<String> getStringResponseEntity(HttpClient client, HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(response.statusCode()).body(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }finally {
            client = null;
            System.gc();
        }
    }

    /**
     * Utility method that returns a Post-HttpRequest
     * @param url the url
     * @param json the body
     * @return the HttpRequest
     */
    private HttpRequest.Builder getPostRequestWithBody(String url, String json){
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json");
    }

    /**
     * Utility method that returns a Put-HttpRequest
     * @param url the url
     * @param json the body
     * @return the HttpRequest
     */
    @NotNull
    private HttpRequest getPutRequestWithBody(String url, String json){
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
    }

    /**
     * Utility method that returns a GET-HttpRequest
     * @param url the url
     * @return the HttpRequest
     */
    private HttpRequest getGetRequest(String url){
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
    }

    /**
     * Utility method that returns a DELETE-HttpRequest
     * @param url the url
     * @return the HttpRequest
     */
    @NotNull
    private HttpRequest getDeleteRequest(String url){
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .DELETE()
            .build();
    }

    /**
     * Utility method that returns an ExecutorService
     * @return the ExecutorService
     */
    private ExecutorService getExecutorService(){
        return Executors.newSingleThreadExecutor();
    }

    /**
     * Utility method that returns an ExecutorService
     * @return the HttpClient
     */
    public HttpClient getHttpClient(){
        return  HttpClient
            .newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(5))
            .executor(getExecutorService())
            .build();
    }
}
