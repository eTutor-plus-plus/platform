package at.jku.dke.etutor.service.client.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.sql.SQLExerciseDTO;
import at.jku.dke.etutor.objects.dispatcher.sql.SQLSchemaInfoDTO;
import at.jku.dke.etutor.objects.dispatcher.sql.SqlDataDefinitionDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.Objects;

@Service
public final class SqlClient extends AbstractDispatcherClient {
    public SqlClient(ApplicationProperties properties) {
        super(properties);
    }
    /**
     * Sends the DDL-Statements for creating an SQL-schema for an SQL-task-group to the dispatcher
     * @param ddl the statements
     * @return an response entity
     */
    public SQLSchemaInfoDTO executeDDLForSQL(SqlDataDefinitionDTO ddl) throws DispatcherRequestFailedException {
        HttpRequest request = null;
        try {
            request = getPostRequestWithBody("/sql/schema", serialize(ddl)).build();
            return deserialize(getResponseEntity(request, stringHandler).getBody(), SQLSchemaInfoDTO.class);
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not serialize the DDL-Statements");
        }
    }

    /**
     * Sends the PUT-request for creating an SQL-exercise to the dispatcher
     * @param solution the solution for the exercise
     * @param schemaName the schema/task-group
     * @return a ResponseEntity
     */
    public Integer createSQLExercise(String solution, String schemaName) throws DispatcherRequestFailedException {
        var exerciseDTO = new SQLExerciseDTO(schemaName, solution);
        HttpRequest request = null;
        try {
            request = getPutRequestWithBody("/sql/exercise", serialize(exerciseDTO));
            return Integer.parseInt(Objects.requireNonNull(getResponseEntity(request, stringHandler).getBody()));
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not serialize the exercise");
        }
    }

    /**
     * Sends the GET-request for retrieving the solution for an SQL-exercise to the dispatcher
     * @return a ResponseEntity
     */
    public String getSQLSolution(int id) throws DispatcherRequestFailedException {
        var request = getGetRequest("/sql/exercise/"+id+"/solution");

        return getResponseEntity(request, stringHandler).getBody();
    }

    /**
     * Sends the request to update the solution of an existing exercise to the dispatcher
     * @param id the id
     * @param newSolution the new solution
     * @return a ResponseEntity as received by the dispatcher
     */
    public Integer updateSQLExerciseSolution(int id, String newSolution) throws DispatcherRequestFailedException {
        var request = getPostRequestWithBody("/sql/exercise/"+id+"/solution", newSolution).build();

        return Integer.parseInt(Objects.requireNonNull(getResponseEntity(request, stringHandler).getBody()));
    }

    /**
     * Sends the request to delete a schema to the dispatcher
     *
     * @param schemaName the schema
     */
    public void deleteSQLSchema(String schemaName) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/sql/schema/"+encodeValue(schemaName));
        getResponseEntity(request, stringHandler);
    }


    /**
     * Sends the reuqest for deleting a connection associated with a given schema to the dispatcher
     *
     * @param schemaName the schema
     */
    public void deleteSQLConnection(String schemaName) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/sql/schema/"+encodeValue(schemaName)+"/connection");
        getResponseEntity(request, stringHandler);
    }

    /**
     * Sends the request to delete an exercise to the dispatcher
     *
     * @param id the exercise-id
     */
    public void deleteSQLExercise(int id) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/sql/exercise/"+id);
        getResponseEntity(request, stringHandler);
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
