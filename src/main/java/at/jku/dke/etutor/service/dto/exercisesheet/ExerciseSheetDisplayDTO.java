package at.jku.dke.etutor.service.dto.exercisesheet;

/**
 * DTO class which represents an exercise sheet.
 *
 * @author fne
 */
public class ExerciseSheetDisplayDTO {
    private String internalId;
    private String name;
    private int individualAssignmentCnt;

    /**
     * Constructor.
     */
    @SuppressWarnings("unused")
    public ExerciseSheetDisplayDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param internalId              the internal id
     * @param name                    the name
     * @param individualAssignmentCnt the individual assignment count
     */
    public ExerciseSheetDisplayDTO(String internalId, String name, int individualAssignmentCnt) {
        this.internalId = internalId;
        this.name = name;
        this.individualAssignmentCnt = individualAssignmentCnt;
    }

    /**
     * Returns the internal id.
     *
     * @return the internal id
     */
    public String getInternalId() {
        return internalId;
    }

    /**
     * Sets the internal id
     *
     * @param internalId the internal id to set
     */
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the individual assignment count.
     *
     * @return the individual assignment count
     */
    public int getIndividualAssignmentCnt() {
        return individualAssignmentCnt;
    }

    /**
     * Sets the individual assignment count.
     *
     * @param individualAssignmentCnt the individual assignment count
     */
    public void setIndividualAssignmentCnt(int individualAssignmentCnt) {
        this.individualAssignmentCnt = individualAssignmentCnt;
    }
}
