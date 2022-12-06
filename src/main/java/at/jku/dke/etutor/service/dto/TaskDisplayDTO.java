package at.jku.dke.etutor.service.dto;

import at.jku.dke.etutor.service.dto.permission.PermissionDTO;

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
    private PermissionDTO permissions;

    /**
     * Constructor.
     */
    public TaskDisplayDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param taskId          the task's id
     * @param header          the task's header
     * @param internalCreator the task's internal creator
     * @param privateTask     the private task indicator
     * @param permissions     the permission object
     */
    public TaskDisplayDTO(String taskId, String header, String internalCreator, boolean privateTask, PermissionDTO permissions) {
        this.taskId = taskId;
        this.header = header;
        this.internalCreator = internalCreator;
        this.privateTask = privateTask;
        this.permissions = permissions.clone();
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
     * Returns this task display's permissions.
     *
     * @return the permissions
     */
    public PermissionDTO getPermissions() {
        return permissions;
    }

    /**
     * Sets this task display's permissions.
     *
     * @param permissions the permission to set
     */
    public void setPermissions(PermissionDTO permissions) {
        this.permissions = permissions.clone();
    }
}
