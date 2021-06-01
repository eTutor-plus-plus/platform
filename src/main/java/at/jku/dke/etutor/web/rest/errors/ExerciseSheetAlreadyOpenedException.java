package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which indicates that an exercise sheet has already
 * been opened.
 *
 * @author fne
 */
public class ExerciseSheetAlreadyOpenedException extends BadRequestAlertException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public ExerciseSheetAlreadyOpenedException() {
        super("The exercise sheet has already been opened!", "courseManagement", "exerciseSheetAlreadyOpened");
    }
}
