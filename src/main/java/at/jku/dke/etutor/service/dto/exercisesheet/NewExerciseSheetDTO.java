package at.jku.dke.etutor.service.dto.exercisesheet;

import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import at.jku.dke.etutor.service.dto.validation.DifficultyRankingConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    @NotNull
    private List<LearningGoalAssignmentDTO> learningGoals = new ArrayList<>();

    @Min(1)
    private int taskCount;

    private boolean generateWholeExerciseSheet;

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
     * Returns the list of associated learning goal assignments.
     *
     * @return the list of associated learning goal assignments
     */
    public List<LearningGoalAssignmentDTO> getLearningGoals() {
        return learningGoals;
    }

    /**
     * Sets the list of associated learning goal assignments.
     *
     * @param learningGoals the learning goal assignments' list to set
     */
    public void setLearningGoals(List<LearningGoalAssignmentDTO> learningGoals) {
        this.learningGoals = learningGoals;
    }

    /**
     * Returns the task count.
     *
     * @return the task count
     */
    public int getTaskCount() {
        return taskCount;
    }

    /**
     * Sets the task count.
     *
     * @param taskCount the task count
     */
    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    /**
     * Adds a learning goal assignment.
     *
     * @param learningGoalAssignment the learning goal assignment to add, not null
     */
    @JsonIgnore
    public void addLearningGoal(LearningGoalAssignmentDTO learningGoalAssignment) {
        if (learningGoals == null) {
            learningGoals = new ArrayList<>();
        }
        if (learningGoalAssignment != null) {
            learningGoals.add(learningGoalAssignment);
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

    /**
     * Returns whether or not a whole exercise sheet should be generated.
     *
     * @return {@code true} if the whole exercise sheet should be generated, otherwise {@code false}
     */
    public boolean isGenerateWholeExerciseSheet() {
        return generateWholeExerciseSheet;
    }

    /**
     * Sets whether or not a whole exercise sheet should be generated
     *
     * @param generateWholeExerciseSheet {@code true} if the whole exercise sheet should be generated, otherwise {@code false}
     */
    public void setGenerateWholeExerciseSheet(boolean generateWholeExerciseSheet) {
        this.generateWholeExerciseSheet = generateWholeExerciseSheet;
    }
}
