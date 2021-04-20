package at.jku.dke.etutor.service.dto.validation;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import one.util.streamex.StreamEx;

/**
 * Class which is used to validate difficulty ranking ids.
 *
 * @author fne
 */
public class DifficultyRankingValidator implements ConstraintValidator<DifficultyRankingConstraint, String> {

    private String[] availableIds;

    /**
     * Constructor.
     */
    public DifficultyRankingValidator() {
        availableIds = new String[4];

        availableIds[0] = ETutorVocabulary.Easy.getURI();
        availableIds[1] = ETutorVocabulary.Medium.getURI();
        availableIds[2] = ETutorVocabulary.Hard.getURI();
        availableIds[3] = ETutorVocabulary.VeryHard.getURI();
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
