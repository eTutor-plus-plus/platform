package at.jku.dke.etutor.service.dto.exercisesheet;

import java.time.Instant;

/**
 * DTO class for an existing exercise sheet.
 *
 * @author fne
 */
public class ExerciseSheetDTO extends NewExerciseSheetDTO {
    private String id;
    private Instant creationDate;
    private String internalCreator;

    /**
     * Empty constructor.
     */
    public ExerciseSheetDTO() {
        // For serialization
    }

    /**
     * Constructor.
     *
     * @param baseDTO the base dto
     * @param id the generated id
     * @param creationDate the creation date
     * @param internalCreator the internal creator
     */
    public ExerciseSheetDTO(NewExerciseSheetDTO baseDTO, String id, Instant creationDate, String internalCreator) {
        setName(baseDTO.getName());
        setDifficultyId(baseDTO.getDifficultyId());
        setLearningGoals(baseDTO.getLearningGoals());

        this.id = id;
        this.creationDate = creationDate;
        this.internalCreator = internalCreator;
    }

    /**
     * Returns the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the creation date.
     *
     * @return the creation date
     */
    public Instant getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creation date
     *
     * @param creationDate the creation date to set
     */
    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the internal creator.
     *
     * @return the internal creator
     */
    public String getInternalCreator() {
        return internalCreator;
    }

    /**
     * Sets the internal creator.
     *
     * @param internalCreator the internal creator to set
     */
    public void setInternalCreator(String internalCreator) {
        this.internalCreator = internalCreator;
    }
}
