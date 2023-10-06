package at.jku.dke.etutor.service.tasktypes.client.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.processmining.PmExerciseConfigDTO;
import at.jku.dke.etutor.objects.dispatcher.processmining.PmExerciseLogDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;

@Service
public final class PmClient extends AbstractDispatcherClient {

    public PmClient(ApplicationProperties properties) {
        super(properties);
    }

    /**
     * Requests the creation of a pm exercise configuration -> sends the PUT-request for creating an Pm exercise config to the dispatcher
     * @param exerciseConfigDTO the {@link PmExerciseConfigDTO} wrapping the configuration information
     * @return an {@link ResponseEntity} wrapping the assigned configuration id
     * @throws DispatcherRequestFailedException
     */
    public ResponseEntity<Integer> createPmExerciseConfiguration(PmExerciseConfigDTO exerciseConfigDTO) throws DispatcherRequestFailedException{
        String path = "/pm/configuration";
        HttpRequest request = null;

        try{
            request = getPutRequestWithBody(path, new ObjectMapper().writeValueAsString(exerciseConfigDTO));
        }catch(JsonProcessingException e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(-1);
        }

        var response = getResponseEntity(request, stringHandler);
        if(response.getBody() == null){
            throw new DispatcherRequestFailedException("No id has returned by the dispatcher");
        }

        var id = Integer.parseInt(response.getBody());
        return ResponseEntity.status(response.getStatusCodeValue()).body(id);
    }

    /**
     * Sends the request to update the parameters of an existing configuration to the dispatcher
     * @param id the configuration id
     * @param exerciseConfigDTO the parameters
     * @return a ResponseEntitiy as received by the dispatcher
     */
    public ResponseEntity<String> updatePmExerciseConfiguration(int id, PmExerciseConfigDTO exerciseConfigDTO) throws DispatcherRequestFailedException{
        String path = "/pm/configuration/"+id+"/values";
        HttpRequest request = null;

        try {
            request = getPostRequestWithBody(path, new ObjectMapper().writeValueAsString(exerciseConfigDTO)).build();
        }catch (JsonProcessingException e){
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
        return getResponseEntity(request, stringHandler);
    }

    /**
     * Deletes a Process Mining Exercise Configuration
     * @param id the exercise id
     * @return a Response Entity
     * @throws DispatcherRequestFailedException
     */
    public ResponseEntity<String> deletePmExerciseConfiguration(int id) throws DispatcherRequestFailedException{
        String path = "/pm/configuration/"+id;
        var request = getDeleteRequest(path);

        return getResponseEntity(request, stringHandler);
    }

    /**
     * Requests information about a pm exercise configuration from the dispatcher
     * @param id the id of the configuration
     * @return the {@link PmExerciseConfigDTO} wrapping the information
     * @throws DispatcherRequestFailedException
     */
    public ResponseEntity<PmExerciseConfigDTO> getPmExerciseConfiguration(int id) throws DispatcherRequestFailedException{
        String path = "/pm/configurations/"+id;
        var request = getGetRequest(path);
        var response = getResponseEntity(request,stringHandler);
        PmExerciseConfigDTO exerciseConfigDTO = null;

        try{
            exerciseConfigDTO = new ObjectMapper().readValue(response.getBody(), PmExerciseConfigDTO.class);
        }catch (JsonProcessingException e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(new PmExerciseConfigDTO());
        }
        return ResponseEntity.status(response.getStatusCodeValue()).body(exerciseConfigDTO);
    }

    /**
     * Requests information about a pm exercise log from the dispatcher
     * @param exerciseId the exercise id corresponding to the requested log
     * @return the {@link PmExerciseLogDTO} wrapping the information
     * @throws DispatcherRequestFailedException
     */
    public ResponseEntity<PmExerciseLogDTO> fetchLogToExercise(int exerciseId) throws DispatcherRequestFailedException{
        String path = "/pm/log/"+exerciseId;
        var request = getGetRequest(path);
        var response = getResponseEntity(request, stringHandler);
        PmExerciseLogDTO pmExerciseLogDTO = null;

        try{
            pmExerciseLogDTO = new ObjectMapper().readValue(response.getBody(), PmExerciseLogDTO.class);
        }catch(JsonProcessingException e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(new PmExerciseLogDTO());
        }
        return ResponseEntity.status(response.getStatusCodeValue()).body(pmExerciseLogDTO);
    }

    /**
     * Requests the creation of a random exercise -> sends the GET- request for creating a random pm exercise
     * based on the configuration id
     * @param configId the configuration id passed by the eTutor
     * @return {@link ResponseEntity} wrapping the assigned pm exercise id
     * @throws DispatcherRequestFailedException
     */
    public ResponseEntity<Integer> createRandomPmExercise(int configId) throws DispatcherRequestFailedException{
        String path = "/pm/exercise/"+configId;
        var request = getGetRequest(path);
        var response = getResponseEntity(request, stringHandler);

        if(response.getBody() == null){
            throw new DispatcherRequestFailedException("No id has returned by the dispatcher");
        }
        var id = Integer.parseInt(response.getBody());
        return ResponseEntity.status(response.getStatusCodeValue()).body(id);
    }
}
