package at.jku.dke.etutor.service;

import at.jku.dke.etutor.objects.dispatcher.GradingDTO;
import at.jku.dke.etutor.objects.dispatcher.SubmissionDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.tasktypes.proxy.bpmn.BpmnSubmissionProxyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BpmnDispatcherSubmissionService {
    private final BpmnSubmissionProxyService bpmnSubmissionProxyService;
    private final ObjectMapper mapper = new ObjectMapper();

    public BpmnDispatcherSubmissionService(BpmnSubmissionProxyService bpmnSubmissionProxyService) {
        this.bpmnSubmissionProxyService = bpmnSubmissionProxyService;
    }

    /**
     * Returns the {@link SubmissionDTO} for a given id, which represents a submission for an individual task
     *
     * @param UUID the UUID
     * @return the Bpmn submission
     * @throws JsonProcessingException if the returned value cannot be deserialized
     */
    public SubmissionDTO getBpmnSubmission(String UUID) throws JsonProcessingException, DispatcherRequestFailedException {
        return mapper.readValue(bpmnSubmissionProxyService.getBpmnSubmission(UUID).getBody(), SubmissionDTO.class);
    }

    /**
     * Returns the {@link GradingDTO} for a given id, representing a graded submission for an individual task
     *
     * @param UUID the UUID
     * @return the grading
     * @throws JsonProcessingException if the returned value cannot be parsed
     */
    public GradingDTO getBpmnGrading(String UUID) throws JsonProcessingException, DispatcherRequestFailedException {
        return mapper.readValue(bpmnSubmissionProxyService.getBpmnGrading(UUID).getBody(), GradingDTO.class);
    }

    /**
     * Returns the submission string for a given submission, identified by its UUID.
     * Also checks if the passed UUID is an actual UUID.
     * @param submissionUUID the UUID (dispatcher) for the submission
     * @return the submission string
     */
    public Optional<String> getSubmissionStringFromSubmissionUUID(String submissionUUID) {
        try {
            UUID.fromString(submissionUUID); // check if submission is valid UUID; throws Exception if not (legacy requirement)
            var submissionDTO = getBpmnSubmission(submissionUUID);

            return Optional.ofNullable(new ObjectMapper().writeValueAsString(submissionDTO.getPassedAttributes()));
        } catch (IllegalArgumentException | JsonProcessingException | DispatcherRequestFailedException ex) {
            // Todo: handle exceptions
        }
        return Optional.of(submissionUUID); //legacy requirement; some persisted submissions might not be UUIDs, but actual submission strings
    }
}
