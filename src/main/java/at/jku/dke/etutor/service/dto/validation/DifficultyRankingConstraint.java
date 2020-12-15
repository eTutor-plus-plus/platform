package at.jku.dke.etutor.service.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation for the difficulty ranking validation.
 *
 * @author fne
 */
@Documented
@Constraint(validatedBy = DifficultyRankingValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DifficultyRankingConstraint {
    String message() default "Invalid course type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
