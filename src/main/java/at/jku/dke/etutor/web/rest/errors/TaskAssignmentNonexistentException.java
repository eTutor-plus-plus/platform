package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which is thrown when a task assignment does not exist.
 *
 * @author fne
 */
public class TaskAssignmentNonexistentException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public TaskAssignmentNonexistentException() {
        super("The task assignment does not exist!", "taskAssignmentManagement", "taskAssignmentNotFound");
    }
}
