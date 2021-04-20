package at.jku.dke.etutor.service.exception;

import java.io.Serial;

/**
 * Validation exception which is thrown when the student CSV file is
 * in an invalid format.
 *
 * @author fne
 */
public class StudentCSVImportException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;
}
