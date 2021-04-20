package at.jku.dke.etutor.service.exception;

/**
 * Validation exception which is thrown when a goal which should be marked as public
 * has a private super goal.
 *
 * @author fne
 */
public class PrivateSuperGoalException extends Exception {

    private static final long serialVersionUID = 1L;
}
