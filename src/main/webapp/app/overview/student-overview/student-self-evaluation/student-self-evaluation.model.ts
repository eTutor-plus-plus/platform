import { FormGroup } from '@angular/forms';

/**
 * Interface which represents a self evaluation learning goal.
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

/**
 * Interface which represents a self evaluation learning goal with an optional parent reference as well as
 * a list of sub (aka child) goals.
 */
export interface IStudentSelfEvaluationLearningGoalWithReference extends IStudentSelfEvaluationLearningGoal {
  /**
   * The list of sub goals.
   */
  subGoals: IStudentSelfEvaluationLearningGoalWithReference[];
  /**
   * Marks the assigned form group.
   */
  group: FormGroup;
  /**
   * The optional parent goal.
   */
  parentGoal?: IStudentSelfEvaluationLearningGoalWithReference;
}
