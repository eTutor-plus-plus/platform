/**
 * Interface which represents the statistics overview model
 * of a single course instance.
 */
export interface IStatisticsOverviewModelDTO {
  /**
   * The course instance's name.
   */
  courseInstanceName: string;
  /**
   * The total count of students.
   */
  studentCount: number;
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
