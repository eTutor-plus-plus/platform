package at.jku.dke.etutor.service.exception;

import java.io.Serial;

/**
 * Validation exception which is thrown when a student
 * does not exists.
 *
 * @author fne
 */
public class StudentNotExistsException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
}
