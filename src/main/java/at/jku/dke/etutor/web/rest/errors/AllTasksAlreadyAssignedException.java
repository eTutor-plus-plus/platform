package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which indicates that all available tasks of an individual exercise sheet assignment
 * are already assigned.
 *
 * @author fne
 */
public class AllTasksAlreadyAssignedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AllTasksAlreadyAssignedException() {
        super("All tasks have already been assigned!", "courseManagement", "allTasksAssigned");
    }
}
