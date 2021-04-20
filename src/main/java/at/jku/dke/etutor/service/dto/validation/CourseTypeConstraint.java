package at.jku.dke.etutor.service.dto.validation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation for the course type validation.
 *
 * @author fne
 */
@Documented
@Constraint(validatedBy = CourseTypeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CourseTypeConstraint {
    String message() default "Invalid course type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
