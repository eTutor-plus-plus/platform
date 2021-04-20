package at.jku.dke.etutor.web.rest.errors;

/**
 * Validation exception which is thrown when a learning goal assignment already exists.
 *
 * @author fne
 */
public class LearningGoalAssignmentAlreadyExistsException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public LearningGoalAssignmentAlreadyExistsException() {
        super("The learning goal assignment already exists!", "courseManagement", "learningGoalAssignmentAlreadyExists");
    }
}
