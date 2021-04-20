package at.jku.dke.etutor.service.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * Dto class for the setting of learning goal assignments.
 */
public class LearningGoalUpdateAssignmentDTO {

    @NotBlank
    private String courseId;

    @NotEmpty
    private List<String> learningGoalIds = new ArrayList<>();

    /**
     * Empty constructor for jackson.
     */
    public LearningGoalUpdateAssignmentDTO() {
        //Empty for jackson
    }

    /**
     * Returns the course id.
     *
     * @return the course id
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Sets the course id.
     *
     * @param courseId the course id to set
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * Returns the list of associated learning goal ids.
     *
     * @return the list of associated learning goal ids
     */
    public List<String> getLearningGoalIds() {
        return learningGoalIds;
    }

    /**
     * Sets the list of associated learning goal ids.
     *
     * @param learningGoalIds he list of associated learning goal ids to set
     */
    public void setLearningGoalIds(List<String> learningGoalIds) {
        this.learningGoalIds = learningGoalIds;
    }
}
