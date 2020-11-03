package at.jku.dke.etutor.web.rest.errors;

/**
 * Validation exception which will be thrown if a learning goal does not exist!.
 *
 * @author fne
 */
public class LearningGoalNotFoundException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public LearningGoalNotFoundException() {
        super("The learning goal does not exist!", "learningGoalManagement", "learningGoalNotFound");
    }
}
