package at.jku.dke.etutor.service.dto.taskassignment;

import javax.validation.constraints.NotBlank;

/**
 * DTO class for a new task group.
 *
 * @author fne
 */
public class NewTaskGroupDTO {

    @NotBlank
    private String name;
    private String description;

    /**
     * Constructor.
     *
     * @param name        the mandatory name
     * @param description the optional description
     */
    public NewTaskGroupDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Empty constructor.
     */
    public NewTaskGroupDTO() {
        // Empty for serialization
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
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
