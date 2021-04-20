package at.jku.dke.etutor.web.rest.vm;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * View model for storing the grading info.
 */
public class GradingInfoVM {

    @NotBlank
    private String courseInstanceUUID;

    @NotBlank
    private String exerciseSheetUUID;

    @NotBlank
    private String matriculationNo;

    @Min(1)
    private int orderNo;

    private boolean goalCompleted;

    /**
     * Constructor.
     */
    public GradingInfoVM() {
        // Empty for serialization
    }

    /**
     * Returns the course instance uuid.
     *
     * @return the course instance uuid
     */
    public String getCourseInstanceUUID() {
        return courseInstanceUUID;
    }

    /**
     * Sets the course instance uuid.
     *
     * @param courseInstanceUUID the course instance uuid to set
     */
    public void setCourseInstanceUUID(String courseInstanceUUID) {
        this.courseInstanceUUID = courseInstanceUUID;
    }

    /**
     * Returns the exercise sheet uuid.
     *
     * @return the exercise sheet uuid
     */
    public String getExerciseSheetUUID() {
        return exerciseSheetUUID;
    }

    /**
     * Sets the exercise sheet uuid.
     *
     * @param exerciseSheetUUID the exercise sheet uuid to set
     */
    public void setExerciseSheetUUID(String exerciseSheetUUID) {
        this.exerciseSheetUUID = exerciseSheetUUID;
    }

    /**
     * Returns the matriculation number.
     *
     * @return the matriculation number
     */
    public String getMatriculationNo() {
        return matriculationNo;
    }

    /**
     * Sets the matriculation number.
     *
     * @param matriculationNo the matriculation number to set
     */
    public void setMatriculationNo(String matriculationNo) {
        this.matriculationNo = matriculationNo;
    }

    /**
     * Returns the order number.
     *
     * @return the order number
     */
    public int getOrderNo() {
        return orderNo;
    }

    /**
     * Sets the order number.
     *
     * @param orderNo the order number to set
     */
    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    /**
     * Returns whether the goal has been completed or not.
     *
     * @return {@code true} if the goal has been completed, otherwise {@code false}
     */
    public boolean isGoalCompleted() {
        return goalCompleted;
    }

    /**
     * Sets whether the goal has been completed or not.
     *
     * @param goalCompleted the value to set
     */
    public void setGoalCompleted(boolean goalCompleted) {
        this.goalCompleted = goalCompleted;
    }
}
