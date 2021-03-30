package at.jku.dke.etutor.service.dto.courseinstance.taskassignment;

/**
 * DTO class which transfers overview information of student assignments
 */
public class StudentAssignmentOverviewInfoDTO {
    private String matriculationNo;
    private boolean submitted;
    private boolean fullyGraded;

    /**
     * Constructor.
     */
    public StudentAssignmentOverviewInfoDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param matriculationNo the matriculation number
     * @param submitted       indicates whether the assignment has already been submitted or not
     * @param fullyGraded     indicates whether the assignment has already been fully graded or not
     */
    public StudentAssignmentOverviewInfoDTO(String matriculationNo, boolean submitted, boolean fullyGraded) {
        this.matriculationNo = matriculationNo;
        this.submitted = submitted;
        this.fullyGraded = fullyGraded;
    }

    /**
     * Returns the student's matriculation number.
     *
     * @return the student's matriculation number
     */
    public String getMatriculationNo() {
        return matriculationNo;
    }

    /**
     * Sets the student's matriculation number.
     *
     * @param matriculationNo the matriculation number to set
     */
    public void setMatriculationNo(String matriculationNo) {
        this.matriculationNo = matriculationNo;
    }

    /**
     * Returns whether the assignment has already been submitted or not.
     *
     * @return {@code true} if the assignment has already been submitted, otherwise {@code false}
     */
    public boolean isSubmitted() {
        return submitted;
    }

    /**
     * Sets whether the assignment has already been submitted or not.
     *
     * @param submitted the value to set
     */
    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    /**
     * Returns whether the assignment has already been fully graded or not.
     *
     * @return {@code true} if the assignment has already been fully graded, otherwise {@code false}
     */
    public boolean isFullyGraded() {
        return fullyGraded;
    }

    /**
     * Sets whether the assignment has already been fully graded or not.
     *
     * @param fullyGraded the value to set
     */
    public void setFullyGraded(boolean fullyGraded) {
        this.fullyGraded = fullyGraded;
    }
}
