package at.jku.dke.etutor.service.tasktypes.proxy.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.sql.SQLExerciseDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;

@Service
public final class SqlProxyService extends AbstractDispatcherProxyService {
    public SqlProxyService(ApplicationProperties properties) {
        super(properties);
    }
    /**
     * Sends the DDL-Statements for creating an SQL-schema for an SQL-task-group to the dispatcher
     * @param ddl the statements
     * @return an response entity
     */
    public ResponseEntity<String> executeDDLForSQL(String ddl) throws DispatcherRequestFailedException {
        var request = getPostRequestWithBody("/sql/schema", ddl).build();
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
            request = getPutRequestWithBody("/sql/exercise", new ObjectMapper().writeValueAsString(exerciseDTO));
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
    public ResponseEntity<String> getSQLSolution(int id) throws DispatcherRequestFailedException {
        var request = getGetRequest("/sql/exercise/"+id+"/solution");

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the request to update the solution of an existing exercise to the dispatcher
     * @param id the id
     * @param newSolution the new solution
     * @return a ResponseEntity as received by the dispatcher
     */
    public ResponseEntity<String> updateSQLExerciseSolution(int id, String newSolution) throws DispatcherRequestFailedException {
        var request = getPostRequestWithBody("/sql/exercise/"+id+"/solution", newSolution).build();

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the request to delete a schema to the dispatcher
     * @param schemaName the schema
     * @return a ResponseEntity
     */
    public ResponseEntity<String> deleteSQLSchema(String schemaName) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/sql/schema/"+encodeValue(schemaName));
        return getResponseEntity(request, stringHandler);
    }


    /**
     * Sends the reuqest for deleting a connection associated with a given schema to the dispatcher
     * @param schemaName the schema
     * @return a ResponseEntity as received by the dispatcher
     */
    public ResponseEntity<String> deleteSQLConnection(String schemaName) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/sql/schema/"+encodeValue(schemaName)+"/connection");
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the request to delete an exercise to the dispatcher
     * @param id the exercise-id
     * @return the response from the dispatcher
     */
    public ResponseEntity<String> deleteSQLExercise(int id) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/sql/exercise/"+id);
        return getResponseEntity(request, stringHandler);
    }

    public ResponseEntity<String> getHTMLTableForSQL(String tableName, int connId, int exerciseId, String taskGroup) throws DispatcherRequestFailedException {
        String url = "/sql/table/"+encodeValue(tableName);
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
}
