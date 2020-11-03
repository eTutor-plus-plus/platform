package at.jku.dke.etutor.web.rest.errors;

import at.jku.dke.etutor.web.rest.errors.BadRequestAlertException;

/**
 * Validation exception which will be thrown if a learning goal name already exists.
 *
 * @author fne
 */
public class LearningGoalAlreadyExistsException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public LearningGoalAlreadyExistsException() {
        super("The learning goal already exists!", "learningGoalManagement", "learningGoalAlreadyExists");
    }
}
