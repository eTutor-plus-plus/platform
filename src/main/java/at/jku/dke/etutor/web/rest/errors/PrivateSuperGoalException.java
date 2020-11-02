package at.jku.dke.etutor.web.rest.errors;

/**
 * Validation exception which is thrown when a goal which should be marked as public
 * has a private super goal.
 *
 * @author fne
 */
public class PrivateSuperGoalException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public PrivateSuperGoalException() {
        super("A private super goal for a public sub goal is not allowed!", "learningGoalManagement", "privateSuperGoal");
    }
}
