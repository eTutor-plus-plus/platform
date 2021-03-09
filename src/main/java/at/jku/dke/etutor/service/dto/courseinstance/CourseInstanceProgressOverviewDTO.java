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
     */
    public CourseInstanceProgressOverviewDTO(String exerciseSheetId, String assignmentHeader, String difficultyURI, boolean completed) {
        this.exerciseSheetId = exerciseSheetId;
        this.assignmentHeader = assignmentHeader;
        this.difficultyURI = difficultyURI;
        this.completed = completed;
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
}
