package at.jku.dke.etutor.service.dto.taskassignment;

/**
 * Display dto for a task assignment (containing the header and id).
 *
 * @author fne
 */
public class TaskAssignmentDisplayDTO {

    private String header;
    private String id;

    /**
     * Constructor.
     */
    public TaskAssignmentDisplayDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param header the task assignment's header
     * @param id     the task assignment's internal id
     */
    public TaskAssignmentDisplayDTO(String header, String id) {
        this.header = header;
        this.id = id;
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
     * @param header the header name to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Returns the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
