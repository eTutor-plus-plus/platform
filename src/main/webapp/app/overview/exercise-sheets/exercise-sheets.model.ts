import { ILearningGoalDisplayModel } from '../shared/learning-goal-model';

/**
 * Interface which represents a new exercise sheet.
 */
export interface INewExerciseSheetDTO {
  /**
   * The exercise sheet's name.
   */
  name: string;
  /**
   * The difficulty id string
   */
  difficultyId: string;
  /**
   * The learning goals.
   */
  learningGoals: ILearningGoalAssignmentDTO[];
  /**
   * The exercise sheet's task count.
   */
  taskCount: number;
  /**
   * Indicates whether or not the whole exercise sheet should be generated at once.
   */
  generateWholeExerciseSheet: boolean;
}

/**
 * Interface which represents a learning goal assignment.
 */
export interface ILearningGoalAssignmentDTO {
  /**
   * The associated learning goal.
   */
  learningGoal: ILearningGoalDisplayModel;
  /**
   * The priority - must be greater than 0
   */
  priority: number;
}

/**
 * Interface which represents an existing exercise sheet.
 */
export interface IExerciseSheetDTO extends INewExerciseSheetDTO {
  /**
   * The exercise sheet's id.
   */
  id: string;
  /**
   * The creation date.
   */
  creationDate: Date;
  /**
   * The internal creator.
   */
  internalCreator: string;
}

/**
 * Interface which represents a displayable
 * exercise sheet entry.
 */
export interface IExerciseSheetDisplayDTO {
  /**
   * The internal id.
   */
  internalId: string;
  /**
   * The display name.
   */
  name: string;
  /**>
   * The individual assignment count.
   */
  individualAssignmentCnt: number;
  /**
   * Indicates whether or not the exercise sheet has already been closed.
   */
  closed: boolean;
}
