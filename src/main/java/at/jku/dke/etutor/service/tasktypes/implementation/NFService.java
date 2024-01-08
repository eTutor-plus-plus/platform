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

@Service
public class NFService implements TaskTypeService {

    private final NFClient nfClient;

    public NFService(NFClient nfClient) {
        this.nfClient = nfClient;
    }

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
            throw new NFException("Could not create NF exercise because: " + e.getMessage());
        }
    }

    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            NFExerciseDTO nfExerciseDTO = convertToNfExerciseDTO(taskAssignmentDTO);

            nfClient.modifyExercise(id, nfExerciseDTO);
        } catch(NumberFormatException | DispatcherRequestFailedException e){
            throw new NFException("Could not update NF exercise because: " + e.getMessage());
        }
    }

    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        try{
            int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
            nfClient.deleteExercise(id);
        } catch(NumberFormatException | DispatcherRequestFailedException e){
            throw new NFException("Could not delete NF exercise because: " + e.getMessage());
        }
    }

    private NFExerciseDTO convertToNfExerciseDTO(NewTaskAssignmentDTO taskAssignmentDTO) {
        NFExerciseDTO nfExerciseDTO = new NFExerciseDTO();

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
