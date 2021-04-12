/**
 * Interface which represents
 */
export interface IStudentSelfEvaluationLearningGoal {
  /**
   * The internal goal id.
   */
  id: string;
  /**
   * The goal text
   */
  text: string;
  /**
   * Indicates whether the goal has already been completed or not.
   */
  completed: boolean;
}
