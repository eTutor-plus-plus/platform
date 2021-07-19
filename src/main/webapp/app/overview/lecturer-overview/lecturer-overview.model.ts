/**
 * Interface which represents the statistics overview model
 * of a single course instance.
 */
export interface IStatisticsOverviewModelDTO {
  /**
   * List of learning goal achievements.
   */
  learningGoalAchievementOverview: ILearningGoalProgressDTO[];
}

/**
 * Interface which represents the progress of a single learning goal.
 */
export interface ILearningGoalProgressDTO {
  /**
   * The learning goal's id.
   */
  goalId: string;
  /**
   * The learning goal's name.
   */
  goalName: string;
  /**
   * The absolute count of students who reached the goal.
   */
  absoluteCount: number;
  /**
   * The relative count of students who reached the goal.
   */
  relativeCount: number;
}
