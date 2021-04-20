package at.jku.dke.etutor.web.rest.errors;

/**
 * Validation exception which is thrown when a learning goal assignment does not exist.
 *
 * @author fne
 */
public class LearningGoalAssignmentNonExistentException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public LearningGoalAssignmentNonExistentException() {
        super("The learning goal assignment does not exist!", "courseManagement", "learningGoalAssignmentNonExistent");
    }
}
