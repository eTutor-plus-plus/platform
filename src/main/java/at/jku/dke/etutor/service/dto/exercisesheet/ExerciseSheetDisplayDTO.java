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
    private boolean closed;

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
     * @param closed                  indicates whether the exercise sheet has already been closed
     */
    public ExerciseSheetDisplayDTO(String internalId, String name, int individualAssignmentCnt, boolean closed) {
        this.internalId = internalId;
        this.name = name;
        this.individualAssignmentCnt = individualAssignmentCnt;
        this.closed = closed;
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

    /**
     * Returns whether the exercise sheet has already been closed.
     *
     * @return {@code true} if the exercise sheet has already been closed, otherwise {@code false}
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Sets whether the exercise sheet has already been closed.
     *
     * @param closed the value to set
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
