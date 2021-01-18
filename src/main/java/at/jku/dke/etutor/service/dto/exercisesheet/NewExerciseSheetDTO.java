package at.jku.dke.etutor.service.dto.exercisesheet;

import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import at.jku.dke.etutor.service.dto.validation.DifficultyRankingConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO class for a new exercise sheet.
 *
 * @author fne
 */
public class NewExerciseSheetDTO {
    @NotBlank
    private String name;
    @NotBlank
    @DifficultyRankingConstraint
    private String difficultyId;
    @NotEmpty
    private List<LearningGoalDisplayDTO> learningGoals = new ArrayList<>();

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the task difficulty id.
     *
     * @return the task difficulty id
     */
    public String getDifficultyId() {
        return difficultyId;
    }

    /**
     * Sets the task difficulty id.
     *
     * @param difficultyId the task difficulty id to set
     */
    public void setDifficultyId(String difficultyId) {
        this.difficultyId = difficultyId;
    }

    /**
     * Returns the list of associated learning goals.
     *
     * @return the list of associated learning goals
     */
    public List<LearningGoalDisplayDTO> getLearningGoals() {
        return learningGoals;
    }

    /**
     * Sets the list of associated learning goals.
     *
     * @param learningGoals the list to set
     */
    public void setLearningGoals(List<LearningGoalDisplayDTO> learningGoals) {
        this.learningGoals = learningGoals;
    }

    /**
     * Adds a learning goal.
     *
     * @param learningGoal the learning goal to add, not null
     */
    @JsonIgnore
    public void addLearningGoal(LearningGoalDisplayDTO learningGoal) {
        if (learningGoals == null) {
            learningGoals = new ArrayList<>();
        }
        if (learningGoal != null) {
            learningGoals.add(learningGoal);
        }
    }

    /**
     * Removes a learning goal.
     *
     * @param learningGoal the learning goal to remove, not null
     */
    @JsonIgnore
    public void removeLearningGoal(LearningGoalDisplayDTO learningGoal) {
        if (learningGoals != null && learningGoal != null) {
            learningGoals.remove(learningGoal);
        }
    }
}
