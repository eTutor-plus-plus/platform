package at.jku.dke.etutor.service.dto;

import javax.validation.constraints.NotBlank;

/**
 * Dto class for a learning goal assignment.
 */
public class LearningGoalAssignmentDTO {

    @NotBlank
    private String courseId;

    @NotBlank
    private String learningGoalId;

    /**
     * Empty constructor for jackson.
     */
    public LearningGoalAssignmentDTO() {
        // Empty for jackson
    }

    /**
     * Constructor.
     *
     * @param courseId       the id of the course.
     * @param learningGoalId the id of the learning goal
     */
    public LearningGoalAssignmentDTO(String courseId, String learningGoalId) {
        this.courseId = courseId;
        this.learningGoalId = learningGoalId;
    }

    /**
     * Returns the course's id.
     *
     * @return the id of the course
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Sets the course's id.
     *
     * @param courseId the id to set
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * Returns the learning goal's id.
     *
     * @return the id of learning goal
     */
    public String getLearningGoalId() {
        return learningGoalId;
    }

    /**
     * Sets the learning goal's id.
     *
     * @param learningGoalId the id to set
     */
    public void setLearningGoalId(String learningGoalId) {
        this.learningGoalId = learningGoalId;
    }
}
