package at.jku.dke.etutor.service.dto.validation;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import one.util.streamex.StreamEx;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Class which is used to validate task assignment type ids.
 *
 * @author fne
 */
public class TaskAssignmentTypeValidator implements ConstraintValidator<TaskAssignmentTypeConstraint, String> {

    private String[] availableIds;

    /**
     * Constructor.
     */
    public TaskAssignmentTypeValidator() {
        availableIds = new String[7];

        availableIds[0] = ETutorVocabulary.NoType.getURI();
        availableIds[1] = ETutorVocabulary.UploadTask.getURI();
        availableIds[2] = ETutorVocabulary.SQLTask.getURI();
        availableIds[3] = ETutorVocabulary.RATask.getURI();
        availableIds[4] = ETutorVocabulary.XQueryTask.getURI();
        availableIds[5] = ETutorVocabulary.DatalogTask.getURI();
        availableIds[6] = ETutorVocabulary.CalcTask.getURI();

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
