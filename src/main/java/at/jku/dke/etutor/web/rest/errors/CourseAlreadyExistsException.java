package at.jku.dke.etutor.web.rest.errors;

/**
 * Validation exception which will be thrown if a course name already exists.
 *
 * @author fne
 */
public class CourseAlreadyExistsException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public CourseAlreadyExistsException() {
        super("The course already exists!", "courseManagement", "courseAlreadyExists");
    }
}
