package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which indicates a storage error.
 *
 * @author fne
 */
public class FileStorageException extends BadRequestAlertException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public FileStorageException() {
        super("A file storage problem occurred!", "courseManagement", "fileStorage");
    }
}
