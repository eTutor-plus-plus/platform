package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which is thrown when a student csv import error occurs.
 *
 * @author fne
 */
public class StudentCSVImportException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public StudentCSVImportException() {
        super("A CSV import error occurred!", "courseManagement", "studentCSVImportError");
    }
}
