package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which indicates that a student does not exist.
 *
 * @author fne
 */
public class StudentNotExistsException extends BadRequestAlertException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public StudentNotExistsException() {
        super("The student does not exist!", "courseManagement", "studentNotExists");
    }
}
