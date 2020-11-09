package at.jku.dke.etutor.web.rest.errors;

/**
 * Validation exception which will be thrown if a course can not be found.
 *
 * @author fne
 */
public class CourseNotFoundException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public CourseNotFoundException() {
        super("The course does not exist!", "courseManagement", "courseNotFound");
    }
}
