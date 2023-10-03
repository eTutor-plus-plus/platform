package at.jku.dke.etutor.service.tasktypes.proxy;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.sql.SQLExerciseDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.http.HttpRequest;

@Service
public class SqlProxyService extends DispatcherProxyService{
    public SqlProxyService(ApplicationProperties properties) {
        super(properties);
    }
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
}
