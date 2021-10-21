package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.dto.dispatcher.DispatcherXMLDTO;
import at.jku.dke.etutor.service.dto.dispatcher.XQueryExerciseDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import io.swagger.models.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.config.JHipsterDefaults;

import javax.servlet.http.HttpServletRequest;
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<String> postSubmission(@RequestBody String submissionDto, @RequestHeader("Accept-Language") String language){
        var client = getHttpClient();
        var request = getPostRequestWithBody(dispatcherURL+"/submission", submissionDto)
            .setHeader(HttpHeaders.ACCEPT_LANGUAGE, language)
            .build();

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the submission UUID to the dispatcher and returns the submission
     * @param submissionUUID the UUID identifying the submission
     * @return the submission
     */
    @GetMapping(value="/submission/{submissionUUID}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<String> getSubmission(@PathVariable String submissionUUID){
        var client = getHttpClient();
        var request = getGetRequest(dispatcherURL+"/submission/"+submissionUUID);

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the DDL-Statements for creating an SQL-schema for an SQL-task-group to the dispatcher
     * @param ddl the statements
     * @return an response entity
     */
    @PostMapping(value="/sql/schema")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> executeDDLForSQL(@RequestBody String ddl){
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> createSQLExercise(@RequestBody String solution, @PathVariable String schemaName, @PathVariable int id){
        var client = getHttpClient();
        var request = getPutRequestWithBody(dispatcherURL+"/sql/exercise/"+schemaName+"/"+id, solution);

        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the GET-request for getting the solution for an SQL-exercise to the dispatcher
     * @return a ResponseEntity
     */

    @GetMapping(value="/sql/exercise/{id}/solution")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getSQLSolution(@PathVariable int id){
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> updateSQLExerciseSolution(@PathVariable int id, @RequestBody String newSolution){
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> deleteSQLSchema(@PathVariable String schemaName){
        var client = getHttpClient();
        var request = getDeleteRequest(dispatcherURL+"/sql/schema/"+schemaName);
        return getStringResponseEntity(client, request);
    }

    /**
     * Sends the request to get an available exercise-id to the dispatcher
     * @return the id
     */
    @GetMapping(value="/sql/exercise/reservation")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getExerciseIDForSQL(){
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> deleteSQLConnection(@PathVariable String schemaName){
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> deleteSQLExercise(@PathVariable int id){
        var client = getHttpClient();
        var request = getDeleteRequest(dispatcherURL+"/sql/exercise/"+id);
        return getStringResponseEntity(client, request);
    }
    @GetMapping(value="/sql/table/{tableName}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<String> getHTMLTableForSQL(@PathVariable String tableName, @RequestParam(defaultValue="-1") int exerciseId, @RequestParam(defaultValue = "") String taskGroup){
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
     * Sends the POST-request for adding XML-files for a task group to the dispatcher
     * @param taskGroup the UUID for the task group
     * @param dto the dto containing the xml's
     * @return the file id of the created xml file from the dispatcher for retrieving
     */
    @PostMapping("/xquery/xml/taskGroup/{taskGroup}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> addXMLForXQTaskGroup(@PathVariable String taskGroup, @RequestBody String dto){
        String url = dispatcherURL+"/xquery/xml/taskGroup/"+taskGroup;
        var client = getHttpClient();
        var request = getPostRequestWithBody(url, dto).build();
        ResponseEntity<String> responseEntity= getStringResponseEntity(client, request);

        return ResponseEntity.ok(responseEntity.getBody());
    }
    /**
     * Sends the DELETE-request for xml resources for a specific task group to the dispatcher
     * @param taskGroup the UUID for the task group
     * @return the file id of the created xml file from the dispatcher for retrieving
     */
    @DeleteMapping("/xquery/xml/taskGroup/{taskGroup}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> deleteXMLofXQTaskGroup(@PathVariable String taskGroup){
        String url = dispatcherURL+"/xquery/xml/taskGroup/"+taskGroup;
        var client = getHttpClient();
        var request = getDeleteRequest(url);
        ResponseEntity<String> responseEntity= getStringResponseEntity(client, request);

        return ResponseEntity.ok(responseEntity.getBody());
    }

    /**
     * Creates an XQuery exercise
     * @param taskGroup the taskGroup to associate the exercise with
     * @param exercise the exercise
     * @return a ResponseEntity
     */
    @PostMapping("/xquery/exercise/taskGroup/{taskGroup}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Integer> createXQExercise(@PathVariable String taskGroup, @RequestBody String exercise){
        String url = dispatcherURL+"/xquery/exercise/taskGroup/"+taskGroup;
        var client = getHttpClient();
        var request = getPostRequestWithBody(url, exercise);
        HttpResponse<String> response = null;
        try {
            response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.ok(Integer.parseInt(response.body()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(500).body(-1);
    }

    /**
     * Updates an xquery exercise
     * @param id the id of the exercise
     * @return a ResponseEntity
     */
    @PostMapping("/xquery/exercise/id/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> updateXQExercise(@PathVariable int id, @RequestBody String dto){
        String url = dispatcherURL+"/xquery/exercise/id/"+id;
        var client = getHttpClient();
        var request = getPostRequestWithBody(url, dto);

        return getStringResponseEntity(client, request.build());
    }

    /**
     * Returns the solution query and the sortings XPath for an XQuery exercise
     * @param id the task id (dispatcher)
     * @return a ResponseEntity
     */
    @GetMapping("/xquery/exercise/solution/id/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getXQExerciseInfo(@PathVariable int id){
        var url = dispatcherURL + "/xquery/exercise/solution/id/"+id;
        var client = getHttpClient();
        var request = getGetRequest(url);
        return getStringResponseEntity(client, request);
    }

    /**
     * Deletes an XQuery exercise
     * @param id the exercise id
     * @return a ResponseEntity
     */
    @DeleteMapping("/xquery/exercise/id/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
   public ResponseEntity<String> deleteXQExercise(@PathVariable int id){
        String url = dispatcherURL+"/xquery/exercise/id/"+id;

        var client = getHttpClient();
        var request = getDeleteRequest(url);

        return getStringResponseEntity(client, request);
    }

    /**
     * Returns the xml datasource for an xquery taskgroup
     * @param taskGroup the naem of the taskgroup
     * @return a ResponseEntity
     */
    @GetMapping("/xquery/xml/taskGroup/{taskGroup}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<String> getXMLForXQByTaskGroup(@PathVariable String taskGroup){
       String url = dispatcherURL+"xquery/xml/taskGroup/"+taskGroup;
        var client = getHttpClient();
       var request = getGetRequest(url);

       return getStringResponseEntity(client, request);
    }
    /**
     * Returns the xml datasource for an xquery taskgroup
     * @param id the file id of the xml
     * @return a ResponseEntity
     */
    @GetMapping("/xquery/xml/fileid/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<String> getXMLForXQByFileId(@PathVariable int id){
        String url = dispatcherURL+"/xquery/xml/fileid/"+id;
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not proxy request to dispatcher");
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
