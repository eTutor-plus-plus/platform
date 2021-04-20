package at.jku.dke.etutor.service.dto;

/**
 * DTO class for transferring student self evaluations.
 */
public class StudentSelfEvaluationLearningGoalDTO {

    private String id;
    private String text;
    private boolean completed;

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
     * Returns the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text.
     *
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns whether the goal has been reached or not.
     *
     * @return {@code true} if the goal has been reached, otherwise {@code false}
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets whether the goal has been reached or not.
     *
     * @param completed the value to set
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
