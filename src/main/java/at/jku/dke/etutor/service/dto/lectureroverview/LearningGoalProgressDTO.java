package at.jku.dke.etutor.service.dto.lectureroverview;

/**
 * DTO class which represents the progress of a learning goal, i.e.
 * the absolute as well as the relative number of students who have
 * already reached the goal.
 *
 * @author fne
 */
public class LearningGoalProgressDTO {
    private String goalId;
    private String goalName;
    private int absoluteCount;
    private double relativeCount;

    /**
     * Constructor.
     */
    public LearningGoalProgressDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param goalId        the goal's internal id
     * @param goalName      the goal's name
     * @param absoluteCount the absolute count of students that have already reached this goal
     * @param relativeCount the relative count of students that have already reached this goal
     */
    public LearningGoalProgressDTO(String goalId, String goalName, int absoluteCount, double relativeCount) {
        this.goalId = goalId;
        this.goalName = goalName;
        this.absoluteCount = absoluteCount;
        this.relativeCount = relativeCount;
    }

    /**
     * Returns the goal's internal id.
     *
     * @return the internal goal id
     */
    public String getGoalId() {
        return goalId;
    }

    /**
     * Sets the goal id.
     *
     * @param goalId the id to set
     */
    public void setGoalId(String goalId) {
        this.goalId = goalId;
    }

    /**
     * Returns the goal's name.
     *
     * @return the goal's name
     */
    public String getGoalName() {
        return goalName;
    }

    /**
     * Sets the goal's name.
     *
     * @param goalName the name to set
     */
    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    /**
     * Returns the absolute count.
     *
     * @return the absolute count
     */
    public int getAbsoluteCount() {
        return absoluteCount;
    }

    /**
     * Sets the absolute count.
     *
     * @param absoluteCount the absolute count to set
     */
    public void setAbsoluteCount(int absoluteCount) {
        this.absoluteCount = absoluteCount;
    }

    /**
     * Returns the relative count.
     *
     * @return the relative count
     */
    public double getRelativeCount() {
        return relativeCount;
    }

    /**
     * Sets the relative count.
     *
     * @param relativeCount the relative count to set
     */
    public void setRelativeCount(double relativeCount) {
        this.relativeCount = relativeCount;
    }
}
