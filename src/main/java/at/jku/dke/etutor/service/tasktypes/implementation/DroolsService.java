package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.FileEntity;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.sql.SQLSchemaInfoDTO;
import at.jku.dke.etutor.objects.dispatcher.sql.SqlDataDefinitionDTO;
import at.jku.dke.etutor.repository.FileRepository;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.client.dke.DroolsClient;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service that handles all SQL task-type specific operations.
 */
@Service
public class DroolsService implements TaskTypeService {
    private final DroolsClient droolsClient;
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final ApplicationProperties properties;
    private final FileRepository fileRepository;
    public DroolsService(DroolsClient droolsClient,
                         AssignmentSPARQLEndpointService assignmentSPARQLEndpointService,
                         ApplicationProperties properties, FileRepository fileRepository) {
        this.droolsClient = droolsClient;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.properties = properties;
        this.fileRepository = fileRepository;
    }


    /**
     * Creates an Drools task by sending a request to the dispatcher.
     * If the request was successful, the id received from the dispatcher is set for the passed object.
     * @param newTaskAssignmentDTO the task assignment to be created
     * @throws MissingParameterException if the task solution is not set
     * @throws DispatcherRequestFailedException if the request to the dispatcher failed
     */
    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException, NotAValidTaskGroupException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        if(!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.DroolsTask.toString()))
            return;

        if (StringUtils.isBlank(newTaskAssignmentDTO.getDroolsSolution())) {
            throw new MissingParameterException("The task solution is not set");
        }

        if (StringUtils.isBlank(newTaskAssignmentDTO.getDroolsClasses())) {
            throw new MissingParameterException("No classes set for this task");
        }

        if (!(newTaskAssignmentDTO.getDroolsObjectsFileId() >= 0)) {
            throw new MissingParameterException("No object-file id set");
        }

        // Create task
        var optId = this.handleTaskCreation(newTaskAssignmentDTO);

        // Set dispatcher id of task
        newTaskAssignmentDTO.setTaskIdForDispatcher(
            optId.map(String::valueOf)
            .orElseThrow(() -> new DispatcherRequestFailedException("Dispatcher returned invalid id")));
    }


    /**
     * Updates an SQL task by sending a request to the dispatcher.
     *
     * @param taskAssignmentDTO the task assignment to be updated
     * @throws MissingParameterException if the solution is not set
     * @throws DispatcherRequestFailedException if the request to the dispatcher failed
     */
    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        if(StringUtils.isNotBlank(taskAssignmentDTO.getSqlSolution())){
            String solution = taskAssignmentDTO.getSqlSolution();
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            droolsClient.updateSQLExerciseSolution(id, solution);
        }else{
            throw new MissingParameterException("Drools solution is missing");
        }
    }

    /**
     * Deletes an Drools task by sending a request to the dispatcher.
     *
     * @param taskAssignmentDTO the task assignment to be deleted
     */
    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO)  {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            droolsClient.deleteSQLExercise(id);
        }catch(NumberFormatException | DispatcherRequestFailedException ignore){
            // we ignore it if deletion is not successful
        }

    }

    // Private reqion


    /**
     * Creates a Drools task
     *
     * @param newTaskAssignmentDTO the new task assignment
     * @return the id of the created exercise
     */
    private Optional<Integer> handleTaskCreation(NewTaskAssignmentDTO newTaskAssignmentDTO) throws DispatcherRequestFailedException {
        Objects.requireNonNull(newTaskAssignmentDTO.getDroolsSolution());
        Objects.requireNonNull(newTaskAssignmentDTO.getDroolsClasses());
        FileEntity objectFile = fileRepository.findById((long) newTaskAssignmentDTO.getDroolsObjectsFileId()).orElse(new FileEntity());


        // Get solution, classes and objects required by the dispatcher to create the task
        String solution = newTaskAssignmentDTO.getDroolsSolution();
        String classes = newTaskAssignmentDTO.getDroolsClasses();
        String objectsFileContent = new String(objectFile.getContent(), StandardCharsets.UTF_8);
        int maxPoints = Integer.parseInt(newTaskAssignmentDTO.getMaxPoints());

        // Proxy request to dispatcher
        var response = droolsClient.createDroolsExercise(solution, maxPoints, classes, objectsFileContent);

        // Return dispatcher-id of the exercise
        return Optional.of(response);
    }

}
