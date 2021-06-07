package at.jku.dke.etutor.service.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation for the task assignment type validation.
 *
 * @author fne
 */
@Documented
@Constraint(validatedBy = TaskAssignmentTypeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskAssignmentTypeConstraint {
    String message() default "Invalid task assignment type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
