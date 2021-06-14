package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which indicates that a wrong task type has been selected.
 *
 * @author fne
 */
public class WrongTaskTypeException extends BadRequestAlertException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public WrongTaskTypeException() {
        super("The wrong task type has been selected!", "taskAssignmentManagement", "wrongTaskType");
    }
}
