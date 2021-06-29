package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Exception which indicates that a task group is already existent.
 *
 * @author fne
 */
public class TaskGroupAlreadyExistentException extends BadRequestAlertException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public TaskGroupAlreadyExistentException() {
        super("The task group's name is already existent!", "taskManagement", "taskGroupAlreadyExistent");
    }
}
