package at.jku.dke.etutor.service.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation for the course type validation.
 *
 * @author fne
 */
@Documented
@Constraint(validatedBy = CourseTypeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CourseTypeConstraint {
    String message() default "Invalid course type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
