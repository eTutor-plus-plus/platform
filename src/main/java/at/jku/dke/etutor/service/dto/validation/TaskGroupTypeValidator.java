package at.jku.dke.etutor.service.dto.validation;


import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import one.util.streamex.StreamEx;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Class which is used to validate task group type ids.
 */


public class TaskGroupTypeValidator implements ConstraintValidator<TaskGroupTypeConstraint, String> {

    private String[] availableIds;

    /**
     * Constructor.
     */
    public TaskGroupTypeValidator() {
        availableIds = new String[4];

        availableIds[0] = ETutorVocabulary.NoTypeTaskGroup.getURI();
        availableIds[1] = ETutorVocabulary.SQLTypeTaskGroup.getURI();
        availableIds[2] = ETutorVocabulary.XQueryTypeTaskGroup.getURI();
        availableIds[3] = ETutorVocabulary.DatalogTypeTaskGroup.getURI();
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
