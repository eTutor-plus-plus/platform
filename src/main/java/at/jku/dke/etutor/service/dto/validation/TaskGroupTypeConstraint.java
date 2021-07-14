package at.jku.dke.etutor.service.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation for the task group type validation.
 *
 * @author ks
 */
@Documented
@Constraint(validatedBy = TaskGroupTypeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskGroupTypeConstraint {
    String message() default "Invalid task group type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
