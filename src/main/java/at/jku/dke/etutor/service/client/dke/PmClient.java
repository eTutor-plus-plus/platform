package at.jku.dke.etutor.service.client.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.processmining.PmExerciseConfigDTO;
import at.jku.dke.etutor.objects.dispatcher.processmining.PmExerciseLogDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;

/**
 * Client for interacting with the process mining endpoint of the dispatcher.
 */
@Service
public non-sealed class PmClient extends AbstractDispatcherClient {

    public PmClient(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Requests the creation of a pm exercise configuration -> sends the PUT-request for creating an Pm exercise config to the dispatcher
     * @param exerciseConfigDTO the {@link PmExerciseConfigDTO} wrapping the configuration information
     * @return an {@link ResponseEntity} wrapping the assigned configuration id
     * @throws DispatcherRequestFailedException if an error occurs
     */
    public Integer createPmExerciseConfiguration(PmExerciseConfigDTO exerciseConfigDTO) throws DispatcherRequestFailedException{
        String path = "/pm/configuration";
        HttpRequest request = null;

        try{
            request = getPutRequestWithBody(path,serialize(exerciseConfigDTO));
        }catch(JsonProcessingException e){
            throw new DispatcherRequestFailedException("Could not serialize the exercise config");
        }

        var response = getResponseEntity(request, stringHandler, 200);
        if(response.getBody() == null){
            throw new DispatcherRequestFailedException("No id has returned by the dispatcher");
        }

        return Integer.parseInt(response.getBody());
    }

    /**
     * Sends the request to update the parameters of an existing configuration to the dispatcher
     * @param id the configuration id
     * @param exerciseConfigDTO the parameters
     */
    public void updatePmExerciseConfiguration(int id, PmExerciseConfigDTO exerciseConfigDTO) throws DispatcherRequestFailedException{
        String path = "/pm/configuration/"+id+"/values";
        HttpRequest request = null;

        try {
            request = getPostRequestWithBody(path, serialize(exerciseConfigDTO)).build();
        }catch (JsonProcessingException e){
            throw new DispatcherRequestFailedException("Could not serialize the exercise config");
        }
        getResponseEntity(request, stringHandler, 200);
    }

    /**
     * Deletes a Process Mining Exercise Configuration
     * @param id the exercise id
     * @throws DispatcherRequestFailedException if an error occurs
     */
    public void deletePmExerciseConfiguration(int id) throws DispatcherRequestFailedException{
        String path = "/pm/configuration/"+id;
        var request = getDeleteRequest(path);

        getResponseEntity(request, stringHandler, 200);
    }

    /**
     * Requests information about a pm exercise configuration from the dispatcher
     * @param id the id of the configuration
     * @return the {@link PmExerciseConfigDTO} wrapping the information
     * @throws DispatcherRequestFailedException if an error occurs
     */
    public PmExerciseConfigDTO getPmExerciseConfiguration(int id) throws DispatcherRequestFailedException{
        String path = "/pm/configurations/"+id;
        var request = getGetRequest(path);
        var response = getResponseEntity(request,stringHandler, 200);
        PmExerciseConfigDTO exerciseConfigDTO = null;

        try{
            return deserialize(response.getBody(), PmExerciseConfigDTO.class);
        }catch (JsonProcessingException e){
            throw new DispatcherRequestFailedException("Could not deserialize the response body");
        }
    }

    /**
     * Requests information about a pm exercise log from the dispatcher
     * @param exerciseId the exercise id corresponding to the requested log
     * @return the {@link PmExerciseLogDTO} wrapping the information
     * @throws DispatcherRequestFailedException if an error occurs
     */
    public PmExerciseLogDTO fetchLogToExercise(int exerciseId) throws DispatcherRequestFailedException{
        String path = "/pm/log/"+exerciseId;
        var request = getGetRequest(path);
        var response = getResponseEntity(request, stringHandler, 200);

        try{
            return deserialize(response.getBody(), PmExerciseLogDTO.class);
        }catch(JsonProcessingException e){
            throw new DispatcherRequestFailedException("Could not deserialize the response body");
        }
    }

    /**
     * Requests the creation of a random exercise -> sends the GET- request for creating a random pm exercise
     * based on the configuration id
     * @param configId the configuration id passed by the eTutor
     * @return the assigned pm exercise id
     * @throws DispatcherRequestFailedException if an error occurs
     */
    public Integer createRandomPmExercise(int configId) throws DispatcherRequestFailedException{
        String path = "/pm/exercise/"+configId;
        var request = getGetRequest(path);
        var response = getResponseEntity(request, stringHandler, 200);

        if(response.getBody() == null){
            throw new DispatcherRequestFailedException("No id has returned by the dispatcher");
        }
        return Integer.parseInt(response.getBody());
    }
}
