/**
 * Interface for a new learning goal.
 */
export interface INewLearningGoalModel {
  /**
   * The name of the learning goal.
   */
  name: string,
  /**
   * The description of the learning goal.
   */
  description: string,
  /**
   * Whether the learning goal is private or not.
   */
  privateGoal: boolean
}

/**
 * Interface for a learning goal.
 */
export interface ILearningGoalModel extends INewLearningGoalModel {
  /**
   * The owner of the learning goal.
   */
  owner: string,
  /**
   * The last modification date of the learning goal.
   */
  lastModifiedDate: Date,
  /**
   * The sub goals of this learning goal.
   */
  subGoals: ILearningGoalModel[]
}
