package at.jku.dke.etutor.service.dto.taskassignment;

import java.time.Instant;

/**
 * Class for a task assignment.
 *
 * @author fne
 */
public class TaskAssignmentDTO extends NewTaskAssignmentDTO {

    private String id;
    private Instant creationDate;

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

    /**
     * Returns the creation date.
     *
     * @return the creation date
     */
    public Instant getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creation date.
     *
     * @param creationDate the creation date to set
     */
    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }
}
