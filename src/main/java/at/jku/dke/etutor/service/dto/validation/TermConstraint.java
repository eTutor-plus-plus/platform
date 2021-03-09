package at.jku.dke.etutor.service.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation for the term validation.
 *
 * @author fne
 */
@Documented
@Constraint(validatedBy = TermValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TermConstraint {
    String message() default "Invalid term";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
