package at.jku.dke.etutor.service.dto.validation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation for the difficulty ranking validation.
 *
 * @author fne
 */
@Documented
@Constraint(validatedBy = DifficultyRankingValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DifficultyRankingConstraint {
    String message() default "Invalid course type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
