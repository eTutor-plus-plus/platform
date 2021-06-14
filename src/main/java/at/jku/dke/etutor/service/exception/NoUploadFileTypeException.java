package at.jku.dke.etutor.service.exception;

import java.io.Serial;

/**
 * Validation exception which indicates that a task is not of
 * type UploadFile.
 *
 * @author fne
 */
public class NoUploadFileTypeException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
}
