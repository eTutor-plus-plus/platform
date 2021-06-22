package at.jku.dke.etutor.service.dto.taskassignment;

/**
 * DTO class for displaying a task group in a list.
 *
 * @author fne
 */
public class TaskGroupDisplayDTO {
    private String id;
    private String name;

    /**
     * Empty.
     */
    public TaskGroupDisplayDTO() {
        // Empty constructor for serialization
    }

    /**
     * Constructor.
     *
     * @param id the id
     * @param name the name
     */
    public TaskGroupDisplayDTO(String id, String name) {
        this.id = id;
        this.name = name;
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
}
