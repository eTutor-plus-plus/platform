package at.jku.dke.etutor.service.dto.validation;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import one.util.streamex.StreamEx;

/**
 * Class which is used for the term validation.
 *
 * @author fne
 */
public class TermValidator implements ConstraintValidator<TermConstraint, String> {

    private static String[] availableIds;

    static {
        availableIds = new String[2];
        availableIds[0] = ETutorVocabulary.Winter.getURI();
        availableIds[1] = ETutorVocabulary.Summer.getURI();
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
        return StreamEx.of(availableIds).has(value);
    }
}
