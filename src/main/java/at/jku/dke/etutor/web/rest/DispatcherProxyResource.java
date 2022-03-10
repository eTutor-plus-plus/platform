package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.dto.dispatcher.DatalogExerciseDTO;
import at.jku.dke.etutor.service.dto.dispatcher.DatalogTaskGroupDTO;
import at.jku.dke.etutor.service.dto.dispatcher.SQLExerciseDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Proxy that connects the application with the dispatcher that is used to evaluate sql, datalog, xquery and relational algebra exercises
 */
@Configuration
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherProxyResource {
    private final String dispatcherURL;
    private HttpClient client;
    private final HttpResponse.BodyHandler<String> stringHandler = HttpResponse.BodyHandlers.ofString();


    public DispatcherProxyResource(ApplicationProperties properties){
        this.dispatcherURL = properties.getDispatcher().getUrl();
        init();
    }

    private void init(){
        this.client = HttpClient
            .newBuilder()
            .executor(Executors.newFixedThreadPool(20))
            .build();
    }
    // Proxy-Endpoints

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


    @GetMapping(value="/sql/table/{tableName}")
    public ResponseEntity<String> getHTMLTableForSQL(@PathVariable String tableName, @RequestParam(defaultValue = "-1") int connId, @RequestParam(defaultValue="-1") int exerciseId, @RequestParam(defaultValue = "") String taskGroup) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/sql/table/"+encodeValue(tableName);
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
        if(xml == null) throw new DispatcherRequestFailedException();

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

    // Proxy-methods

    /**
     * Sends the DDL-Statements for creating an SQL-schema for an SQL-task-group to the dispatcher
     * @param ddl the statements
     * @return an response entity
     */
    public ResponseEntity<String> executeDDLForSQL(String ddl) throws DispatcherRequestFailedException {
        var request = getPostRequestWithBody(dispatcherURL+"/sql/schema", ddl).build();
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the PUT-request for creating an SQL-exercise to the dispatcher
     * @param solution the solution for the exercise
     * @param schemaName the schema/task-group
     * @return a ResponseEntity
     */
    public ResponseEntity<String> createSQLExercise(String solution, String schemaName) throws DispatcherRequestFailedException {
        var exerciseDTO = new SQLExerciseDTO(schemaName, solution);
        HttpRequest request = null;
        try {
            request = getPutRequestWithBody(dispatcherURL+"/sql/exercise", new ObjectMapper().writeValueAsString(exerciseDTO));
            return getResponseEntity(request, stringHandler);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sends the GET-request for retrieving the solution for an SQL-exercise to the dispatcher
     * @return a ResponseEntity
     */
    public ResponseEntity<String> getSQLSolution(@PathVariable int id) throws DispatcherRequestFailedException {
        var request = getGetRequest(dispatcherURL+"/sql/exercise/"+id+"/solution");

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the request to update the solution of an existing exercise to the dispatcher
     * @param id the id
     * @param newSolution the new solution
     * @return a ResponseEntity as received by the dispatcher
     */
    public ResponseEntity<String> updateSQLExerciseSolution(int id, String newSolution) throws DispatcherRequestFailedException {
        var request = getPostRequestWithBody(dispatcherURL+"/sql/exercise/"+id+"/solution", newSolution).build();

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the request to delete a schema to the dispatcher
     * @param schemaName the schema
     * @return a ResponseEntity
     */
    public ResponseEntity<String> deleteSQLSchema(String schemaName) throws DispatcherRequestFailedException {
        var request = getDeleteRequest(dispatcherURL+"/sql/schema/"+encodeValue(schemaName));
        return getResponseEntity(request, stringHandler);
    }


    /**
     * Sends the reuqest for deleting a connection associated with a given schema to the dispatcher
     * @param schemaName the schema
     * @return a ResponseEntity as received by the dispatcher
     */
    public ResponseEntity<String> deleteSQLConnection(String schemaName) throws DispatcherRequestFailedException {
        var request = getDeleteRequest(dispatcherURL+"/sql/schema/"+encodeValue(schemaName)+"/connection");
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the request to delete an exercise to the dispatcher
     * @param id the exercise-id
     * @return the response from the dispatcher
     */
    public ResponseEntity<String> deleteSQLExercise(int id) throws DispatcherRequestFailedException {
        var request = getDeleteRequest(dispatcherURL+"/sql/exercise/"+id);
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the POST-request for adding XML-files for a task group to the dispatcher
     * @param taskGroup the UUID for the task group
     * @param dto the dto containing the xml's
     * @return the file id of the created xml file from the dispatcher for retrieving
     */
    public ResponseEntity<String> addXMLForXQTaskGroup(String taskGroup, String dto) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/xml/taskGroup/"+encodeValue(taskGroup);
        var request = getPostRequestWithBody(url, dto).build();
        return getResponseEntity(request, stringHandler);
    }
    /**
     * Sends the DELETE-request for xml resources for a specific task group to the dispatcher
     * @param taskGroup the UUID for the task group
     * @return the file id of the created xml file from the dispatcher for retrieving
     */
    public ResponseEntity<String> deleteXMLofXQTaskGroup(String taskGroup) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/xml/taskGroup/"+encodeValue(taskGroup);
        var request = getDeleteRequest(url);
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Creates an XQuery exercise
     * @param taskGroup the taskGroup to associate the exercise with
     * @param exercise the exercise
     * @return a ResponseEntity
     */
    public ResponseEntity<Integer> createXQExercise(String taskGroup, String exercise) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/exercise/taskGroup/"+encodeValue(taskGroup);
        var request = getPostRequestWithBody(url, exercise);
        HttpResponse<String> response = null;
        try {
            response = client.send(request.build(), stringHandler);
            if(response.statusCode() == 500) throw new DispatcherRequestFailedException();
            return ResponseEntity.ok(Integer.parseInt(response.body()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(500).body(-1);
    }

    /**
     * Updates an xquery exercise
     * @param id the id of the exercise
     * @return a ResponseEntity
     */
    public ResponseEntity<String> updateXQExercise(int id, String dto) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/exercise/id/"+id;
        var request = getPostRequestWithBody(url, dto).build();

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Returns the solution query and the sortings XPath for an XQuery exercise
     * @param id the task id (dispatcher)
     * @return a ResponseEntity
     */
    public ResponseEntity<String> getXQExerciseInfo(int id) throws DispatcherRequestFailedException {
        var url = dispatcherURL + "/xquery/exercise/solution/id/"+id;
        var request = getGetRequest(url);
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Deletes an XQuery exercise
     * @param id the exercise id
     * @return a ResponseEntity
     */
   public ResponseEntity<String> deleteXQExercise(int id) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/xquery/exercise/id/"+id;
        var request = getDeleteRequest(url);

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Proxies the request to create a datalog task group to the dispatcher
     * @param groupDTO the {@link DatalogTaskGroupDTO} containing the name and the facts
     * @return a {@link ResponseEntity} wrapping the id of the newly created task group
     */
    public ResponseEntity<Integer> createDLGTaskGroup(String groupDTO) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/datalog/taskgroup";
        var request = getPostRequestWithBody(url, groupDTO).build();
        var response = getResponseEntity(request, stringHandler);

        if(response.getBody() == null) throw new DispatcherRequestFailedException();

        var id = Integer.parseInt(response.getBody());
        return ResponseEntity.status(response.getStatusCodeValue()).body(id);
    }

    /**
     * Proxies the request to update a datalog task group to the dispatcher
     * @param id the dispatcher id of the task group
     * @param newFacts the new facts to be updated
     * @return an {@link ResponseEntity} indicating whether the update has been successful
     */
    public ResponseEntity<Void> updateDLGTaskGroup( int id,  String newFacts) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/datalog/taskgroup/"+id;
        var request = getPostRequestWithBody(url, newFacts).build();
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Requests the deletion of a datalog task group from the dispatcher
     * @param id the id of the group
     * @return a {@link ResponseEntity} indicating if deletion has been successful
     */
    public ResponseEntity<Void> deleteDLGTaskGroup(int id) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/datalog/taskgroup/"+id;

        var request = getDeleteRequest(url);
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Requests the creation of a datalog exercise
     * @param exerciseDTO the {@link DatalogExerciseDTO} wrapping the exercise information
     * @return an {@link ResponseEntity} wrapping the assigned exercise id
     */
    public ResponseEntity<Integer> createDLGExercise(DatalogExerciseDTO exerciseDTO) throws DispatcherRequestFailedException {
       String url = dispatcherURL+"/datalog/exercise";

        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(url, new ObjectMapper().writeValueAsString(exerciseDTO)).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(-1);
        }
        var response = getResponseEntity(request, stringHandler);

        if (response.getBody() == null) throw new DispatcherRequestFailedException();

       var id = Integer.parseInt(response.getBody());
       return ResponseEntity.status(response.getStatusCodeValue()).body(id);
    }

    /**
     * Requests modification of a datalog exercise
     * @param exerciseDTO the {@link DatalogExerciseDTO} with the new attributes
     * @param id the id of the exercise
     * @return a {@link ResponseEntity} indicating if the udpate has been successful
     */
    public ResponseEntity<Void> modifyDLGExercise(DatalogExerciseDTO exerciseDTO,  int id) throws DispatcherRequestFailedException {
        String url = dispatcherURL+"/datalog/exercise/"+id;

        HttpRequest request = null;
        try {
            request = getPostRequestWithBody(url, new ObjectMapper().writeValueAsString(exerciseDTO)).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Requests information about a datalog exercise from the dispatcher
     * @param id the id of the exercise
     * @return the {@link DatalogExerciseDTO} wrapping the information
     */
    public ResponseEntity<DatalogExerciseDTO> getDLGExercise(int id) throws DispatcherRequestFailedException {
        var request = getGetRequest(dispatcherURL+"/datalog/exercise/"+id);
        var response = getResponseEntity(request, stringHandler);
        DatalogExerciseDTO exercise = null;
        try {
            exercise = new ObjectMapper().readValue((String)response.getBody(), DatalogExerciseDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new DatalogExerciseDTO());
        }
        return ResponseEntity.status(response.getStatusCodeValue()).body(exercise);
    }




    /**
     * Deletes resources associated with a given datalog exercise in the dispatcher
     * @param id the id of the datalog exercise
     * @return a {@link ResponseEntity} indicating if deletion has been successful
     */
    public ResponseEntity<Void> deleteDLGExercise(int id) throws DispatcherRequestFailedException {
        var request = getDeleteRequest(dispatcherURL + "/datalog/exercise/" + id);
        return getResponseEntity(request, HttpResponse.BodyHandlers.discarding());
    }


    /**
     * Utility method that sends an HttpRequest and returns the response-body wrapped inside an ResponseEntity<T>
     * @param request the HttpRequest
     * @return the ResponseEntity
     */
    private <T> ResponseEntity<T> getResponseEntity(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws DispatcherRequestFailedException {
        try {
            HttpResponse<T> response = this.client.send(request, handler);
            if (response.statusCode() == 500) throw new DispatcherRequestFailedException();
            return ResponseEntity.status(response.statusCode()).body(response.body());
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
     * Encodes a string for URL compatibility
     * @param value the value to encode
     * @return the encoded value
     */
    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
