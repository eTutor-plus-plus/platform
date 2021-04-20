package at.jku.dke.etutor.service.dto.validation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation for the term validation.
 *
 * @author fne
 */
@Documented
@Constraint(validatedBy = TermValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TermConstraint {
    String message() default "Invalid term";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
