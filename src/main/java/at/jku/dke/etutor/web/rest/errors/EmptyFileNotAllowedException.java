package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which indicates that an uploaded empty file
 * is not allowed.
 *
 * @author fne
 */
public class EmptyFileNotAllowedException extends BadRequestAlertException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public EmptyFileNotAllowedException() {
        super("An empty file is not allowed!", "courseManagement", "emptyFileNotAllowed");
    }
}
