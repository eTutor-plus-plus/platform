package at.jku.dke.etutor.service.client.dke;

import at.jku.dke.etutor.objects.dispatcher.ddl.DDLExerciseDTO;
import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.Objects;

/**
 * Client for interacting with the sql ddl endpoint of the dispatcher.
 */
@Service
public non-sealed class DDLClient extends AbstractDispatcherClient {
    public DDLClient(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Function to send the PUT-request for creating a SQL DDL exercise to the dispatcher
     * @param solution Specifies the solution for the exercise
     * @param insertStatements Specifies the insert statements for the exercise
     * @param tablePoints Specifies the points for the tables
     * @param columnPoints Specifies the points for the columns
     * @param primaryKeyPoints Specifies the points for the primary keys
     * @param foreignKeyPoints Specifies the points for the foreign keys
     * @param constraintPoints Specifies the points for the constraints
     * @return Returns the exercise id from the dispatcher
     * @throws DispatcherRequestFailedException when the exercise could not be serialized
     */
    public Integer createDDLExercise(String solution, String insertStatements, String maxPoints, String tablePoints, String columnPoints, String primaryKeyPoints, String foreignKeyPoints, String constraintPoints) throws DispatcherRequestFailedException {
        DDLExerciseDTO exerciseDTO = new DDLExerciseDTO(solution, insertStatements, maxPoints, tablePoints, columnPoints, primaryKeyPoints, foreignKeyPoints, constraintPoints);
        HttpRequest request;
        try {
            request = getPutRequestWithBody("/ddl/exercise", serialize(exerciseDTO));
            return Integer.parseInt(Objects.requireNonNull(sendRequest(request, stringHandler, 200).getBody()));
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not serialize the exercise");
        }
    }

    /**
     * Function to send the GET-request for retrieving the solution for a SQL DDL exercise to the dispatcher
     * @return a ResponseEntity
     */
    public String getDDLSolution(int id) throws DispatcherRequestFailedException {
        var request = getGetRequest("/ddl/exercise/"+id+"/solution");

        return sendRequest(request, stringHandler, 200).getBody();
    }

    /**
     * Function to send the request to update the solution of an existing SQL DDL exercise to the dispatcher
     * @param id the id
     * @param newSolution the new solution
     */
    public void updateDDLExercise(int id, String newSolution, String insertStatements, String maxPoints, String tablePoints, String columnPoints, String primaryKeyPoints, String foreignKeyPoints, String constraintPoints) throws DispatcherRequestFailedException {
        var exerciseDTO = new DDLExerciseDTO(newSolution, insertStatements, maxPoints, tablePoints, columnPoints, primaryKeyPoints, foreignKeyPoints, constraintPoints);
        HttpRequest request;
        try {
            request = getPostRequestWithBody("/ddl/exercise/"+id, serialize(exerciseDTO)).build();
        } catch (JsonProcessingException e) {
            throw new DispatcherRequestFailedException("Could not serialize the exercise");
        }

        sendRequest(request, stringHandler, 200);
    }

    /**
     * Sends the request to delete a SQL DDL exercise to the dispatcher     *
     * @param id the exercise-id
     */
    public void deleteDDLExercise(int id) throws DispatcherRequestFailedException {
        var request = getDeleteRequest("/ddl/exercise/"+id);
        sendRequest(request, stringHandler, 200);
    }
}
