package at.jku.dke.etutor.service.dto;

/**
 * DTO of a task display i.e. task header + id.
 *
 * @author fne
 */
public class TaskDisplayDTO {
    private String taskId;
    private String header;

    /**
     * Constructor.
     */
    public TaskDisplayDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param taskId the task's id
     * @param header the task's header
     */
    public TaskDisplayDTO(String taskId, String header) {
        this.taskId = taskId;
        this.header = header;
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
}
