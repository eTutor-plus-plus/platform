package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.processmining.PmExerciseConfigDTO;
import at.jku.dke.etutor.objects.dispatcher.processmining.PmExerciseLogDTO;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.client.dke.PmClient;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProcessMiningService implements TaskTypeService {
    private final PmClient processMiningClient;

    public ProcessMiningService(PmClient processMiningClient) {
        this.processMiningClient = processMiningClient;
    }

    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException, NotAValidTaskGroupException {
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.PmTask.toString())) return;

        if(newTaskAssignmentDTO.getMaxActivity() != 0
            && newTaskAssignmentDTO.getMinActivity() != 0
            && newTaskAssignmentDTO.getMaxLogSize() != 0
            && newTaskAssignmentDTO.getMinLogSize() != 0
            && StringUtils.isNotBlank(newTaskAssignmentDTO.getConfigNum())){
            // creates task and returns dispatcher id
            var optId = this.createPmTaskConfiguration(newTaskAssignmentDTO);

            // set Dispatcher id of configuration
            optId.map(String::valueOf).ifPresent(newTaskAssignmentDTO::setTaskIdForDispatcher);
        }else{
            throw new MissingParameterException("Not enough values provided to create a Process Mining Task");
        }
    }

    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        if(StringUtils.isNotBlank(taskAssignmentDTO.getConfigNum()) && taskAssignmentDTO.getMaxActivity() != 0 && taskAssignmentDTO.getMinActivity() != 0 &&
            taskAssignmentDTO.getMaxLogSize() !=0 && taskAssignmentDTO.getMinLogSize() != 0){
            //Initialize DTO from TaskAssignment DTO
            var configDTO = getPmExerciseConfigDTOFromTaskAssignment(taskAssignmentDTO);

            // get id
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());

            // Proxy request to dispatcher
            processMiningClient.updatePmExerciseConfiguration(id, configDTO);
        }else{
            throw new MissingParameterException("ConfigNum, MaxActivity, MinActivity, MaxLogSize or MinLogSize is missing");
        }

    }

    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            processMiningClient.deletePmExerciseConfiguration(id);
        }catch (NumberFormatException e){
            throw new DispatcherRequestFailedException("Dispatcher id is not a number");
        }
    }
    /**
     * Creates a Process Mining Task Configuration in the dispatcher
     * @param newTaskAssignmentDTO the {@link NewTaskAssignmentDTO} to be created
     * @return the dispatcher-id of the task configuration
     * @throws DispatcherRequestFailedException if the request to the dispatcher fails
     */
    private Optional<Integer> createPmTaskConfiguration(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException{
        // get PmExerciseConfigDTO required by the dispatcher to create the configuration
        var pmExerciseConfigDTO = getPmExerciseConfigDTOFromTaskAssignment(newTaskAssignmentDTO);
        // Proxy request to dispatcher
        var response = processMiningClient.createPmExerciseConfiguration(pmExerciseConfigDTO);
        // return dispatcher -id of the exercise configuration
        return response.getBody() != null ? Optional.of(response.getBody()) : Optional.empty();
    }
    /**
     * Utility method that takes a task assignment and initializes a {@link PmExerciseConfigDTO} with the required information
     * @param newTaskAssignmentDTO the {@link NewTaskAssignmentDTO}
     * @return the {@link PmExerciseConfigDTO}
     */
    private PmExerciseConfigDTO getPmExerciseConfigDTOFromTaskAssignment(NewTaskAssignmentDTO newTaskAssignmentDTO){
        // initialize DTO
        PmExerciseConfigDTO exerciseConfigDTO = new PmExerciseConfigDTO();
        // set variables
        exerciseConfigDTO.setMaxActivity(newTaskAssignmentDTO.getMaxActivity());
        exerciseConfigDTO.setMinActivity(newTaskAssignmentDTO.getMinActivity());
        exerciseConfigDTO.setMaxLogSize(newTaskAssignmentDTO.getMaxLogSize());
        exerciseConfigDTO.setMinLogSize(newTaskAssignmentDTO.getMinLogSize());
        exerciseConfigDTO.setConfigNum(newTaskAssignmentDTO.getConfigNum());

        return exerciseConfigDTO;
    }

    public Optional<Integer> createRandomPmTask(int configId) throws DispatcherRequestFailedException {
        // proxy request to dispatcher
        var response = processMiningClient.createRandomPmExercise(configId);
        // return dispatcher id of the random exercise
        return Optional.ofNullable(response.getBody());
    }

    public PmExerciseLogDTO fetchLogToExercise(int exerciseId) throws DispatcherRequestFailedException {
        return processMiningClient.fetchLogToExercise(exerciseId).getBody();
    }
}
