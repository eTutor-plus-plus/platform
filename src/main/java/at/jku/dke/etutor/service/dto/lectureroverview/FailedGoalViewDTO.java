package at.jku.dke.etutor.service.dto.lectureroverview;

/**
 * DTO class which represents a failed goal.
 *
 * @author fne
 */
public class FailedGoalViewDTO {
    private String id;
    private String name;
    private int failureCount;

    /**
     * Constructor.
     */
    public FailedGoalViewDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param id           the id
     * @param name         the name
     * @param failureCount the failure count
     */
    public FailedGoalViewDTO(String id, String name, int failureCount) {
        this.id = id;
        this.name = name;
        this.failureCount = failureCount;
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

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the failure count.
     *
     * @return the failure count
     */
    public int getFailureCount() {
        return failureCount;
    }

    /**
     * Sets the failure count.
     *
     * @param failureCount the failure count to set
     */
    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }
}
