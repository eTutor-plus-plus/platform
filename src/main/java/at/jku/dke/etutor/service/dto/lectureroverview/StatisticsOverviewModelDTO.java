package at.jku.dke.etutor.service.dto.lectureroverview;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO class which represents the statistics overview model
 * of a single course instance.
 *
 * @author fne
 */
public class StatisticsOverviewModelDTO {

    private String courseInstanceName;
    private int studentCount;
    private List<LearningGoalProgressDTO> learningGoalAchievementOverview;
    private List<FailedGoalViewDTO> failedGoalView;

    /**
     * Constructor.
     */
    public StatisticsOverviewModelDTO() {
        learningGoalAchievementOverview = new ArrayList<>();
        failedGoalView = new ArrayList<>();
    }

    /**
     * Returns the course instance's name.
     *
     * @return the course instance's name
     */
    public String getCourseInstanceName() {
        return courseInstanceName;
    }

    /**
     * Sets the course instance's name.
     *
     * @param courseInstanceName the name to set
     */
    public void setCourseInstanceName(String courseInstanceName) {
        this.courseInstanceName = courseInstanceName;
    }

    /**
     * Returns the student count.
     *
     * @return the student count
     */
    public int getStudentCount() {
        return studentCount;
    }

    /**
     * Sets the student count.
     *
     * @param studentCount the student count to set
     */
    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    /**
     * Returns the list of learning goal achievements.
     *
     * @return the list of learning goal achievements
     */
    public List<LearningGoalProgressDTO> getLearningGoalAchievementOverview() {
        return learningGoalAchievementOverview;
    }

    /**
     * Sets the list of learning goal achievements.
     *
     * @param learningGoalAchievementOverview the list to set
     */
    public void setLearningGoalAchievementOverview(List<LearningGoalProgressDTO> learningGoalAchievementOverview) {
        this.learningGoalAchievementOverview = learningGoalAchievementOverview;
    }

    /**
     * Adds a model to the list.
     *
     * @param modelToAdd the model to add
     */
    public void addStatisticOverviewModel(LearningGoalProgressDTO modelToAdd) {
        if (learningGoalAchievementOverview != null) {
            learningGoalAchievementOverview.add(modelToAdd);
        }
    }

    /**
     * Returns the failed goals view list.
     *
     * @return the failed goals view list
     */
    public List<FailedGoalViewDTO> getFailedGoalView() {
        return failedGoalView;
    }

    /**
     * Sets the failed goal view list.
     *
     * @param failedGoalView the list to set
     */
    public void setFailedGoalView(List<FailedGoalViewDTO> failedGoalView) {
        this.failedGoalView = failedGoalView;
    }

    /**
     * Adds an item to the list of failed goals.
     *
     * @param item the item to add
     */
    public void addFailedGoalViewItem(FailedGoalViewDTO item) {
        if (failedGoalView != null) {
            failedGoalView.add(item);
        }
    }
}
