package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.objects.dispatcher.nf.NFExerciseDTO;
import at.jku.dke.etutor.service.client.dke.NFClient;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.NFException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Service for handling the nf-type specific exercise management
 */
@Service
public class NFService implements TaskTypeService {

    private final NFClient nfClient;

    public NFService(NFClient nfClient) {
        this.nfClient = nfClient;
    }

    /**
     * Creates a new exercise from the supplied <code>NewTaskAssignmentDTO</code>. If no assignment text is specified,
     * an auto-generated one is requested from the dispatcher.
     * @param newTaskAssignmentDTO The <code>NewTaskAssignmentDTO</code> with the content of the new exercise
     * @throws TaskTypeSpecificOperationFailedException If the creation of the exercise failed
     * @throws NotAValidTaskGroupException Never, because the NF module currently does not support task groups.
     */
    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException, NotAValidTaskGroupException {
        try{
            NFExerciseDTO nfExerciseDTO = convertToNfExerciseDTO(newTaskAssignmentDTO);

            int id = nfClient.createExercise(nfExerciseDTO);

            if(id < 0) {
                throw new DispatcherRequestFailedException("Error on dispatcher side when creating new NF exercise");
            }

            newTaskAssignmentDTO.setTaskIdForDispatcher(String.valueOf(id));

            if(StringUtils.isBlank(newTaskAssignmentDTO.getInstruction())) {
                newTaskAssignmentDTO.setInstruction(nfClient.getAssignmentText(id));
            }
        } catch(NumberFormatException | DispatcherRequestFailedException e) {
            throw new NFException(e.getMessage());
        }
    }

    /**
     * Replaces the specified exercise with one specified in the supplied <code>TaskAssignmentDTO</code>.
     * @param taskAssignmentDTO The <code>TaskAssignmentDTO</code> whose content is to replace the existing exercise
     * @throws TaskTypeSpecificOperationFailedException If the replacement of the exercise failed
     */
    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            NFExerciseDTO nfExerciseDTO = convertToNfExerciseDTO(taskAssignmentDTO);

            nfClient.modifyExercise(id, nfExerciseDTO);
        } catch(NumberFormatException | DispatcherRequestFailedException e){
            throw new NFException(e.getMessage());
        }
    }

    /**
     * Deletes the exercise with the specified id.
     * @param taskAssignmentDTO The <code>TaskAssignmentDTO</code> describing the exercise to be deleted
     * @throws TaskTypeSpecificOperationFailedException If the deletion of the exercise failed
     */
    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            nfClient.deleteExercise(id);
        } catch(NumberFormatException | DispatcherRequestFailedException e){
            throw new NFException(e.getMessage());
        }
    }

    /**
     * Converts the supplied <code>NewTaskAssignmentDTO</code> into an <code>NFExerciseDTO</code>.
     * @param taskAssignmentDTO The <code>NewTaskAssignmentDTO</code> to be converted into an <code>NFExerciseDTO</code>
     * @return The <code>NFExerciseDTO</code> generated from the supplied <code>NewTaskAssignmentDTO</code>
     */
    private NFExerciseDTO convertToNfExerciseDTO(NewTaskAssignmentDTO taskAssignmentDTO) {
        NFExerciseDTO nfExerciseDTO = new NFExerciseDTO();

        nfExerciseDTO.setNfBaseRelationName(taskAssignmentDTO.getNfBaseRelationName());
        nfExerciseDTO.setNfBaseAttributes(taskAssignmentDTO.getNfBaseAttributes());
        nfExerciseDTO.setNfBaseDependencies(taskAssignmentDTO.getNfBaseDependencies());
        nfExerciseDTO.setNfTaskSubtypeId(taskAssignmentDTO.getNfTaskSubtypeId());

        switch (nfExerciseDTO.getNfTaskSubtypeId()) {
            case "http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#KeysDeterminationTask" -> {
                nfExerciseDTO.setNfKeysDeterminationPenaltyPerMissingKey(taskAssignmentDTO.getNfKeysDeterminationPenaltyPerMissingKey());
                nfExerciseDTO.setNfKeysDeterminationPenaltyPerIncorrectKey(taskAssignmentDTO.getNfKeysDeterminationPenaltyPerIncorrectKey());
            }
            case "http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#AttributeClosureTask" -> {
                nfExerciseDTO.setNfAttributeClosureBaseAttributes(taskAssignmentDTO.getNfAttributeClosureBaseAttributes());

                nfExerciseDTO.setNfAttributeClosurePenaltyPerMissingAttribute(taskAssignmentDTO.getNfAttributeClosurePenaltyPerMissingAttribute());
                nfExerciseDTO.setNfAttributeClosurePenaltyPerIncorrectAttribute(taskAssignmentDTO.getNfAttributeClosurePenaltyPerIncorrectAttribute());
            }
            case "http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#MinimalCoverTask" -> {
                nfExerciseDTO.setNfMinimalCoverPenaltyPerNonCanonicalDependency(taskAssignmentDTO.getNfMinimalCoverPenaltyPerNonCanonicalDependency());
                nfExerciseDTO.setNfMinimalCoverPenaltyPerTrivialDependency(taskAssignmentDTO.getNfMinimalCoverPenaltyPerTrivialDependency());
                nfExerciseDTO.setNfMinimalCoverPenaltyPerExtraneousAttribute(taskAssignmentDTO.getNfMinimalCoverPenaltyPerExtraneousAttribute());
                nfExerciseDTO.setNfMinimalCoverPenaltyPerRedundantDependency(taskAssignmentDTO.getNfMinimalCoverPenaltyPerRedundantDependency());
                nfExerciseDTO.setNfMinimalCoverPenaltyPerMissingDependencyVsSolution(taskAssignmentDTO.getNfMinimalCoverPenaltyPerMissingDependencyVsSolution());
                nfExerciseDTO.setNfMinimalCoverPenaltyPerIncorrectDependencyVsSolution(taskAssignmentDTO.getNfMinimalCoverPenaltyPerIncorrectDependencyVsSolution());
            }
            case "http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#NormalformDeterminationTask" -> {
                nfExerciseDTO.setNfNormalFormDeterminationPenaltyForIncorrectOverallNormalform(taskAssignmentDTO.getNfNormalFormDeterminationPenaltyForIncorrectOverallNormalform());
                nfExerciseDTO.setNfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform(taskAssignmentDTO.getNfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform());
            }
            case "http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#NormalizationTask" -> {
                nfExerciseDTO.setNfNormalizationTargetLevel(taskAssignmentDTO.getNfNormalizationTargetLevel());
                nfExerciseDTO.setNfNormalizationMaxLostDependencies(taskAssignmentDTO.getNfNormalizationMaxLostDependencies());

                nfExerciseDTO.setNfNormalizationPenaltyPerLostAttribute(taskAssignmentDTO.getNfNormalizationPenaltyPerLostAttribute());
                nfExerciseDTO.setNfNormalizationPenaltyForLossyDecomposition(taskAssignmentDTO.getNfNormalizationPenaltyForLossyDecomposition());
                nfExerciseDTO.setNfNormalizationPenaltyPerNonCanonicalDependency(taskAssignmentDTO.getNfNormalizationPenaltyPerNonCanonicalDependency());
                nfExerciseDTO.setNfNormalizationPenaltyPerTrivialDependency(taskAssignmentDTO.getNfNormalizationPenaltyPerTrivialDependency());
                nfExerciseDTO.setNfNormalizationPenaltyPerExtraneousAttributeInDependencies(taskAssignmentDTO.getNfNormalizationPenaltyPerExtraneousAttributeInDependencies());
                nfExerciseDTO.setNfNormalizationPenaltyPerRedundantDependency(taskAssignmentDTO.getNfNormalizationPenaltyPerRedundantDependency());
                nfExerciseDTO.setNfNormalizationPenaltyPerExcessiveLostDependency(taskAssignmentDTO.getNfNormalizationPenaltyPerExcessiveLostDependency());
                nfExerciseDTO.setNfNormalizationPenaltyPerMissingNewDependency(taskAssignmentDTO.getNfNormalizationPenaltyPerMissingNewDependency());
                nfExerciseDTO.setNfNormalizationPenaltyPerIncorrectNewDependency(taskAssignmentDTO.getNfNormalizationPenaltyPerIncorrectNewDependency());
                nfExerciseDTO.setNfNormalizationPenaltyPerMissingKey(taskAssignmentDTO.getNfNormalizationPenaltyPerMissingKey());
                nfExerciseDTO.setNfNormalizationPenaltyPerIncorrectKey(taskAssignmentDTO.getNfNormalizationPenaltyPerIncorrectKey());
                nfExerciseDTO.setNfNormalizationPenaltyPerIncorrectNFRelation(taskAssignmentDTO.getNfNormalizationPenaltyPerIncorrectNFRelation());
            }
        }

        return nfExerciseDTO;
    }
}
