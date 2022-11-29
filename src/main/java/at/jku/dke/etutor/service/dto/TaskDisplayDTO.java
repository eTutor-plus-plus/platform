package at.jku.dke.etutor.service.dto;

/**
 * DTO of a task display i.e. task header + id.
 *
 * @author fne
 */
public class TaskDisplayDTO {

    private String taskId;
    private String header;
    private String internalCreator;
    private boolean privateTask;
    private boolean currentUserAllowedToEdit;

    /**
     * Constructor.
     */
    public TaskDisplayDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param taskId                   the task's id
     * @param header                   the task's header
     * @param internalCreator          the task's internal creator
     * @param privateTask              the private task indicator
     * @param currentUserAllowedToEdit indicates whether the current user is allowed to edit the displayed task
     */
    public TaskDisplayDTO(String taskId, String header, String internalCreator, boolean privateTask, boolean currentUserAllowedToEdit) {
        this.taskId = taskId;
        this.header = header;
        this.internalCreator = internalCreator;
        this.privateTask = privateTask;
        this.currentUserAllowedToEdit = currentUserAllowedToEdit;
    }

    /**
     * Returns the task id.
     *
     * @return the task id
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Sets the task id.
     *
     * @param taskId the task id to set
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
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
     * Returns the internal creator.
     *
     * @return the internal creator
     */
    public String getInternalCreator() {
        return internalCreator;
    }

    /**
     * Sets the internal creator.
     *
     * @param internalCreator the internal creator to set
     */
    public void setInternalCreator(String internalCreator) {
        this.internalCreator = internalCreator;
    }

    /**
     * Sets whether this task is a private task or not.
     *
     * @return {@code true}, if this task is a private task, otherwise {@code false}
     */
    public boolean isPrivateTask() {
        return privateTask;
    }

    /**
     * Sets whether this task is a private task or not.
     *
     * @param privateTask the value to set
     */
    public void setPrivateTask(boolean privateTask) {
        this.privateTask = privateTask;
    }

    /**
     * Returns whether the current user is allowed to edit this task or not.
     *
     * @return {@code true}, if the current user is allowed to edit this task, otherwise {@code false}
     */
    public boolean isCurrentUserAllowedToEdit() {
        return currentUserAllowedToEdit;
    }

    /**
     * Sets whether the current user is allowed to edit this task or not.
     *
     * @param currentUserAllowedToEdit the value to set
     */
    public void setCurrentUserAllowedToEdit(boolean currentUserAllowedToEdit) {
        this.currentUserAllowedToEdit = currentUserAllowedToEdit;
    }
}
