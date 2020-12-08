package at.jku.dke.etutor.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.codec.Charsets;

import javax.validation.constraints.NotBlank;
import java.net.URLEncoder;

/**
 * A DTO representing a new learning goal.
 */
public class NewLearningGoalDTO {

    @NotBlank
    private String name;
    private String description;
    private boolean privateGoal;

    /**
     * Returns the name of the new learning goal.
     *
     * @return the name of the new learning goal
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name for RDF usage.
     *
     * @return the name prepared for RDF usage
     */
    @JsonIgnore
    public String getNameForRDF() {
        if (name == null) {
            return null;
        }
        return URLEncoder.encode(name.replace(' ', '_').trim(), Charsets.UTF_8);
    }

    /**
     * Sets the name of the new learning goal.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of the new learning goal.
     *
     * @return the description of the new learning goal
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the new learning goal.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns whether this new goal is a private goal or not.
     *
     * @return {@code true} if the goal should be private, otherwise {@code false}
     */
    public boolean isPrivateGoal() {
        return privateGoal;
    }

    /**
     * Sets whether this new goal is a private goal or not.
     *
     * @param privateGoal {@code true} if the goal should be private, otherwise {@code false}
     */
    public void setPrivateGoal(boolean privateGoal) {
        this.privateGoal = privateGoal;
    }
}
