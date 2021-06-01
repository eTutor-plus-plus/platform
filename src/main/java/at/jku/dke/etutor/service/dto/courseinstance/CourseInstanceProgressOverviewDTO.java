package at.jku.dke.etutor.service.dto.courseinstance;

/**
 * DTO class which represents the progress on an exercise
 * sheet.
 *
 * @author fne
 */
public class CourseInstanceProgressOverviewDTO {

    private String exerciseSheetId;
    private String assignmentHeader;
    private String difficultyURI;
    private boolean completed;
    private boolean opened;
    private int actualCount;
    private int submissionCount;
    private int gradedCount;
    private boolean closed;

    /**
     * Constructor.
     */
    public CourseInstanceProgressOverviewDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param exerciseSheetId  the exercise sheet id
     * @param assignmentHeader the assignment header
     * @param difficultyURI    the difficulty URI
     * @param completed        the status
     * @param opened           indicates whether the exercise sheet has already been opened or not
     * @param actualCount      the number of actual tasks
     * @param submissionCount  the number of submitted tasks
     * @param gradedCount      the number of graded tasks
     * @param closed           indicates whether the exercise sheet has already been closed
     */
    public CourseInstanceProgressOverviewDTO(String exerciseSheetId, String assignmentHeader, String difficultyURI, boolean completed, boolean opened,
                                             int actualCount, int submissionCount, int gradedCount, boolean closed) {
        this.exerciseSheetId = exerciseSheetId;
        this.assignmentHeader = assignmentHeader;
        this.difficultyURI = difficultyURI;
        this.completed = completed;
        this.opened = opened;
        this.actualCount = actualCount;
        this.submissionCount = submissionCount;
        this.gradedCount = gradedCount;
        this.closed = closed;
    }

    /**
     * Returns the exercise sheet id.
     *
     * @return the exercise sheet id
     */
    public String getExerciseSheetId() {
        return exerciseSheetId;
    }

    /**
     * Sets the exercise sheet id.
     *
     * @param exerciseSheetId the exercise sheet id to set
     */
    public void setExerciseSheetId(String exerciseSheetId) {
        this.exerciseSheetId = exerciseSheetId;
    }

    /**
     * Returns the assignment header.
     *
     * @return the assignment header
     */
    public String getAssignmentHeader() {
        return assignmentHeader;
    }

    /**
     * Sets the assignment header.
     *
     * @param assignmentHeader the assignment header to set
     */
    public void setAssignmentHeader(String assignmentHeader) {
        this.assignmentHeader = assignmentHeader;
    }

    /**
     * Returns the difficulty URI.
     *
     * @return the difficulty URI
     */
    public String getDifficultyURI() {
        return difficultyURI;
    }

    /**
     * Sets the difficulty URI.
     *
     * @param difficultyURI the difficulty URI to set
     */
    public void setDifficultyURI(String difficultyURI) {
        this.difficultyURI = difficultyURI;
    }

    /**
     * Returns whether the currently logged-in student completed the assignment or not.
     *
     * @return {@code true} if the student completed the assignment, otherwise {@code false}
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets whether the assignment is completed or not.
     *
     * @param completed the value to set
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Returns whether the exercise sheet has already been opened or not not.
     *
     * @return {@code true} if the exercise sheet has already been opened, otherwise {@code false}
     */
    public boolean isOpened() {
        return opened;
    }

    /**
     * Sets whether the exercise sheet has already been opened or not.
     *
     * @param opened the value to set
     */
    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    /**
     * Returns the actual task count.
     *
     * @return the actual task count
     */
    public int getActualCount() {
        return actualCount;
    }

    /**
     * Sets the actual task count.
     *
     * @param actualCount the actual task count to set
     */
    public void setActualCount(int actualCount) {
        this.actualCount = actualCount;
    }

    /**
     * Returns the submitted task count.
     *
     * @return the submitted task count
     */
    public int getSubmissionCount() {
        return submissionCount;
    }

    /**
     * Sets the submitted task count.
     *
     * @param submissionCount the submitted task count to set
     */
    public void setSubmissionCount(int submissionCount) {
        this.submissionCount = submissionCount;
    }

    /**
     * Returns the graded task count.
     *
     * @return the graded task count
     */
    public int getGradedCount() {
        return gradedCount;
    }

    /**
     * Sets the graded task count
     *
     * @param gradedCount the graded task count to set
     */
    public void setGradedCount(int gradedCount) {
        this.gradedCount = gradedCount;
    }

    /**
     * Returns whether the exercise sheet has already been closed or not.
     *
     * @return {@code true} if the exercise sheet has already been closed, otherwise {@code false}
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Sets whether the exercise sheet has already been closed or not.
     *
     * @param closed the value to set
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
