package at.jku.dke.etutor.service.dto.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import one.util.streamex.StreamEx;

/**
 * Class which is used to validate a course type.
 *
 * @author fne
 */
public class CourseTypeValidator implements ConstraintValidator<CourseTypeConstraint, String> {

    private static final String[] AVAILABLE_COURSE_TYPES = { "LVA", "Klasse", "Modul", "Fach" };

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
        if (value == null) return false;
        return StreamEx.of(AVAILABLE_COURSE_TYPES).has(value);
    }
}
