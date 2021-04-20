package at.jku.dke.etutor.service.dto.taskassignment;

import java.util.Objects;
import javax.validation.constraints.NotEmpty;

/**
 * DTO class which contains the learning goal's id and name.
 *
 * @author fne
 */
public class LearningGoalDisplayDTO {

    @NotEmpty
    private String id;

    private String name;

    /**
     * Constructor.
     */
    public LearningGoalDisplayDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param id   the id
     * @param name the name
     */
    public LearningGoalDisplayDTO(String id, String name) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningGoalDisplayDTO that = (LearningGoalDisplayDTO) o;
        return id.equals(that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
