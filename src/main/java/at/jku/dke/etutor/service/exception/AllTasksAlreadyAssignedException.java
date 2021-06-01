package at.jku.dke.etutor.service.exception;

import java.io.Serial;

/**
 * Exception which indicates that all available tasks of an individual exercise sheet assignment
 * are already assigned.
 *
 * @author fne
 */
public class AllTasksAlreadyAssignedException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
}
