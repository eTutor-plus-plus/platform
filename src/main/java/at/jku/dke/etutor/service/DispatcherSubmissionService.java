package at.jku.dke.etutor.service;

import at.jku.dke.etutor.objects.dispatcher.GradingDTO;
import at.jku.dke.etutor.objects.dispatcher.SubmissionDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.tasktypes.client.dke.DkeSubmissionClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for fetching submissions and gradings
 * from the DKE component via the proxy service.
 */
@Service
public class DispatcherSubmissionService {
    private final DkeSubmissionClient dkeSubmissionClient;
    private final ObjectMapper mapper;
    public DispatcherSubmissionService(DkeSubmissionClient dkeSubmissionClient){
        this.dkeSubmissionClient = dkeSubmissionClient;
        this.mapper = new ObjectMapper();
    }

    /**
     * Returns the {@link SubmissionDTO} for a given id, which represents a submission for an individual task
     *
     * @param UUID the UUID
     * @return the submission
     * @throws JsonProcessingException if the returned value cannot be deserialized
     */
    public SubmissionDTO getSubmission(String UUID) throws JsonProcessingException, DispatcherRequestFailedException {
        return mapper.readValue(dkeSubmissionClient.getSubmission(UUID).getBody(), SubmissionDTO.class);
    }


    /**
     * Returns the {@link GradingDTO} for a given id, representing a graded submission for an individual task
     *
     * @param UUID the UUID
     * @return the grading
     * @throws JsonProcessingException if the returned value cannot be parsed
     */
    public GradingDTO getGrading(String UUID) throws JsonProcessingException, DispatcherRequestFailedException {
        return mapper.readValue(dkeSubmissionClient.getGrading(UUID).getBody(), GradingDTO.class);
    }

    /**
     * Returns the submission string for a given submission, identified by its UUID.
     * Also checks if the passed UUID is an actual UUID.
     * @param submissionUUID the UUID (dispatcher) for the submission
     * @param taskAssignmentTypeId the task assignment type
     * @return the submission string
     */
    public Optional<String> getSubmissionStringFromSubmissionUUID(String submissionUUID, String taskAssignmentTypeId) {
        String taskAssignmentType = taskAssignmentTypeId.substring(taskAssignmentTypeId.indexOf("#")+1);
        try {
            UUID.fromString(submissionUUID); // check if submission is valid UUID; throws Exception if not (legacy requirement)
            var submissionDTO = getSubmission(submissionUUID);

            var submissionString = getSubmissionStringFromSubmissionDTO(Objects.requireNonNull(submissionDTO), taskAssignmentType);
            return Optional.ofNullable(submissionString);
        } catch (IllegalArgumentException | JsonProcessingException | DispatcherRequestFailedException ex) {
            // Todo: handle exceptions
        }
        return Optional.of(submissionUUID); //legacy requirement; some persisted submissions might not be UUIDs, but actual submission strings
    }

    /**
     * Returns, for a given submission-dto, the submission-string (e.g. the SQL-Query)
     * @param submissionDTO the dto
     * @param taskAssignmentType the taskassignment-type, to determine how the submission-string has to be extracted from the DTO
     * @return the submission-string
     */
    private String getSubmissionStringFromSubmissionDTO(SubmissionDTO submissionDTO, String taskAssignmentType){
        if(taskAssignmentType.equals("PmTask")){
            try {
                return new ObjectMapper()
                    .writeValueAsString(submissionDTO.getPassedAttributes());
            } catch (JsonProcessingException e) {
                //TODO: Logger
                return "";
            }
        }else{
            return submissionDTO.getPassedAttributes().get("submission");
        }
    }
}
