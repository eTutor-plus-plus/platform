package at.jku.dke.etutor.service.dto.taskassignment;

import at.jku.dke.etutor.service.dto.validation.DifficultyRankingConstraint;

import javax.validation.constraints.NotBlank;
import java.net.URL;

/**
 * Class for a new task assignment.
 *
 * @author fne
 */
public class NewTaskAssignmentDTO {

    @NotBlank
    private String learningGoalId;
    @NotBlank
    private String creator;
    @NotBlank
    private String header;
    private String processingTime;
    @NotBlank
    @DifficultyRankingConstraint
    private String taskDifficultyId;
    @NotBlank
    private String organisationUnit;
    private URL url;
    private String instruction;
    private boolean privateTask;

    /**
     * Returns the associated learning goal id.
     *
     * @return the associated learning goal id
     */
    public String getLearningGoalId() {
        return learningGoalId;
    }

    /**
     * Sets the associated learning goal id.
     *
     * @param learningGoalId the associated learning goal id to set
     */
    public void setLearningGoalId(String learningGoalId) {
        this.learningGoalId = learningGoalId;
    }

    /**
     * Returns the creator.
     *
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the creator.
     *
     * @param creator the creator
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Returns the header.
     *
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * Sets the header.
     *
     * @param header the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Returns the processing time.
     *
     * @return the processing time
     */
    public String getProcessingTime() {
        return processingTime;
    }

    /**
     * Sets the processing time.
     *
     * @param processingTime the processing time to set
     */
    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Returns the corresponding task difficulty id.
     *
     * @return the corresponding task difficulty id
     */
    public String getTaskDifficultyId() {
        return taskDifficultyId;
    }

    /**
     * Sets the task difficulty id.
     *
     * @param taskDifficultyId the task difficulty id to set
     */
    public void setTaskDifficultyId(String taskDifficultyId) {
        this.taskDifficultyId = taskDifficultyId;
    }

    /**
     * Returns the organisation unit.
     *
     * @return the organisation unit
     */
    public String getOrganisationUnit() {
        return organisationUnit;
    }

    /**
     * Sets the organisation unit.
     *
     * @param organisationUnit the organisation unit
     */
    public void setOrganisationUnit(String organisationUnit) {
        this.organisationUnit = organisationUnit;
    }

    /**
     * Returns the task's url.
     *
     * @return the task's url
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the task's url.
     *
     * @param url the task's url to set
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Returns the instructions.
     *
     * @return the instructions
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * Sets the instructions.
     *
     * @param instruction the instruction to set
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    /**
     * Returns whether the task is a private task or not.
     *
     * @return {@code} true if the task is a private task, otherwise {@code false}
     */
    public boolean isPrivateTask() {
        return privateTask;
    }

    /**
     * Sets whether the task is a private task or not.
     *
     * @param privateTask {@code} true if the task is a private task, otherwise {@code false}
     */
    public void setPrivateTask(boolean privateTask) {
        this.privateTask = privateTask;
    }
}
