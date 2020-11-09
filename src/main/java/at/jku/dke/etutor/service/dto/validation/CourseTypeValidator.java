package at.jku.dke.etutor.service.dto.validation;

import one.util.streamex.StreamEx;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Class which is used to validate a course type.
 *
 * @author fne
 */
public class CourseTypeValidator implements ConstraintValidator<CourseTypeConstraint, String> {

    private static final String[] AVAILABLE_COURSE_TYPES = {"LVA", "Klasse", "Modul", "Fach"};

    /**
     * Initializes the validator in preparation for
     * {@link #isValid(String, ConstraintValidatorContext)} calls.
     * The constraint annotation for a given constraint declaration
     * is passed.
     * <p>
     * This method is guaranteed to be called before any use of this instance for
     * validation.
     * <p>
     * The default implementation is a no-op.
     *
     * @param constraintAnnotation annotation instance for a given constraint declaration
     */
    @Override
    public void initialize(CourseTypeConstraint constraintAnnotation) {
    }

    /**
     * Implements the validation logic.
     * The state of {@code value} must not be altered.
     * <p>
     * This method can be accessed concurrently, thread-safety must be ensured
     * by the implementation.
     *
     * @param value   object to validate
     * @param context context in which the constraint is evaluated
     * @return {@code false} if {@code value} does not pass the constraint
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null)
            return false;
        return StreamEx.of(AVAILABLE_COURSE_TYPES).has(value);
    }
}
