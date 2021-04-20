package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Validation exception which will be thrown
 * if a course instance can not be found.
 *
 * @author fne
 */
public class CourseInstanceNotFoundException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public CourseInstanceNotFoundException() {
        super("The course instance can not be found!", "courseManagement", "courseInstanceNotFound");
    }
}
