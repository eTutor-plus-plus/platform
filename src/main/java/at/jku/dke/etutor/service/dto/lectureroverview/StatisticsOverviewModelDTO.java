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
    private List<StatisticsOverviewModelDTO> learningGoalAchievementOverview;

    /**
     * Constructor.
     */
    public StatisticsOverviewModelDTO() {
        learningGoalAchievementOverview = new ArrayList<>();
    }

    /**
     * Returns the list of learning goal achievements.
     *
     * @return the list of learning goal achievements
     */
    public List<StatisticsOverviewModelDTO> getLearningGoalAchievementOverview() {
        return learningGoalAchievementOverview;
    }

    /**
     * Sets the list of learning goal achievements.
     *
     * @param learningGoalAchievementOverview the list to set
     */
    public void setLearningGoalAchievementOverview(List<StatisticsOverviewModelDTO> learningGoalAchievementOverview) {
        this.learningGoalAchievementOverview = learningGoalAchievementOverview;
    }

    /**
     * Adds a model to the list.
     *
     * @param modelToAdd the model to add
     */
    public void addStatisticOverviewModel(StatisticsOverviewModelDTO modelToAdd) {
        if (learningGoalAchievementOverview != null) {
            learningGoalAchievementOverview.add(modelToAdd);
        }
    }
}
