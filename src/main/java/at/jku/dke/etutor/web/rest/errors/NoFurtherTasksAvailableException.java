package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Exception which indicates that now further tasks are available for
 * task allocation.
 *
 * @author fne
 */
public class NoFurtherTasksAvailableException extends BadRequestAlertException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public NoFurtherTasksAvailableException() {
        super("No further tasks are available for allocation!", "courseManagement", "noFurtherTasksAvailable");
    }
}
