package at.jku.dke.etutor.service.exception;

import java.io.Serial;

/**
 * Validation exception which will be thrown if a course instance
 * can not be found.
 *
 * @author fne
 */
public class CourseInstanceNotFoundException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;
}
