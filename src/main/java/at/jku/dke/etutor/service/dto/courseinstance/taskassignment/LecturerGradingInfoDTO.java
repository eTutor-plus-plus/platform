package at.jku.dke.etutor.service.dto.courseinstance.taskassignment;

/**
 * DTO class which is used to transfer grading infos for lecturers.
 *
 * @author fne
 */
public class LecturerGradingInfoDTO {

    private String taskURL;
    private String taskTitle;
    private boolean completed;
    private boolean graded;
    private int orderNo;
    private boolean submitted;
    private String taskTypeId;

    /**
     * Constructor.
     */
    public LecturerGradingInfoDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param taskURL    the task url
     * @param taskTitle  the task title
     * @param completed  indicates whether the task's goals are completed
     * @param graded     indicates whether the task is graded or not
     * @param orderNo    the order number
     * @param submitted  indicates whether the assignment has already been submitted or not
     * @param taskTypeId the task type's id
     */
    public LecturerGradingInfoDTO(String taskURL, String taskTitle, boolean completed, boolean graded, int orderNo, boolean submitted, String taskTypeId) {
        this.taskURL = taskURL;
        this.taskTitle = taskTitle;
        this.completed = completed;
        this.graded = graded;
        this.orderNo = orderNo;
        this.submitted = submitted;
        this.taskTypeId = taskTypeId;
    }

    /**
     * Returns the task url.
     *
     * @return the task url
     */
    public String getTaskURL() {
        return taskURL;
    }

    /**
     * Sets the task url.
     *
     * @param taskURL the task url to set
     */
    public void setTaskURL(String taskURL) {
        this.taskURL = taskURL;
    }

    /**
     * Returns the task title.
     *
     * @return the task title
     */
    public String getTaskTitle() {
        return taskTitle;
    }

    /**
     * Sets the task title.
     *
     * @param taskTitle the task title to set
     */
    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    /**
     * Returns whether the task's goals are completed or not.
     *
     * @return {@code true} if the goals are completed, otherwise {@code false}
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets whether the task's goals are completed or not
     *
     * @param completed the value to set
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Returns whether this task is graded or not.
     *
     * @return {@code true} if this task is graded, otherwise {@code false}
     */
    public boolean isGraded() {
        return graded;
    }

    /**
     * Sets whether the task is graded or not.
     *
     * @param graded the value to set
     */
    public void setGraded(boolean graded) {
        this.graded = graded;
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
     * Returns whether the assignment is submitted or not
     *
     * @return {@code true} if the assignment is submitted, otherwise {@code false}
     */
    public boolean isSubmitted() {
        return submitted;
    }

    /**
     * Sets whether the assignment is submitted or not.
     *
     * @param submitted the value to set
     */
    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    /**
     * Returns the task type's id
     *
     * @return the task type's id
     */
    public String getTaskTypeId() {
        return taskTypeId;
    }

    /**
     * Sets the task type's id.
     *
     * @param taskTypeId the task type's id to set
     */
    public void setTaskTypeId(String taskTypeId) {
        this.taskTypeId = taskTypeId;
    }
}
