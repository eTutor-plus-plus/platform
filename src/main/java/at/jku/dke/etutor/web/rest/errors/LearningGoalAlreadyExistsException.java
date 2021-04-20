package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which will be thrown if a learning goal name already exists.
 *
 * @author fne
 */
public class LearningGoalAlreadyExistsException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public LearningGoalAlreadyExistsException() {
        super("The learning goal already exists!", "learningGoalManagement", "learningGoalAlreadyExists");
    }
}
