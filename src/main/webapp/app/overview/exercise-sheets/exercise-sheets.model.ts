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
  learningGoals: ILearningGoalDisplayModel[];
  /**
   * The exercise sheet's task count.
   */
  taskCount: number;
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
}
