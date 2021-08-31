package at.jku.dke.etutor.service.dto;

/**
 * DTO class for a course overview.
 *
 * @author fne
 */
public class CourseOverviewDTO {

    private String id;
    private String name;

    /**
     * Constructor.
     */
    public CourseOverviewDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param id   the course id
     * @param name the course name
     */
    public CourseOverviewDTO(String id, String name) {
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
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }
}
