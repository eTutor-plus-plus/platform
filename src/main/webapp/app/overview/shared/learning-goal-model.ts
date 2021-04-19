import { LearningGoalTreeItem } from './learning-goal-treeview-item.model';

/**
 * Interface for a new learning goal.
 */
export interface INewLearningGoalModel {
  /**
   * The name of the learning goal.
   */
  name: string;
  /**
   * The description of the learning goal.
   */
  description?: string;
  /**
   * Whether the learning goal is private or not.
   */
  privateGoal: boolean;
  /**
   * Indicates whether the learning goal needs additional verification
   * (only used when the goal is a super goal, i.e. the goal contains sub goals).
   */
  needVerification: boolean;
}

/**
 * Interface for a learning goal.
 */
export interface ILearningGoalModel extends INewLearningGoalModel {
  /**
   * The id of the learning goal.
   */
  id: string;
  /**
   * The owner of the learning goal.
   */
  owner: string;
  /**
   * The last modification date of the learning goal.
   */
  lastModifiedDate: Date;
  /**
   * The sub goals of this learning goal.
   */
  subGoals: ILearningGoalModel[];
  /**
   * The count of lectures which hold a reference to this learning goal.
   */
  referencedFromCount: number;
}

/**
 * Interface for a learning goal display model.
 */
export interface ILearningGoalDisplayModel {
  /**
   * The learning goal's id.
   */
  id: string;
  /**
   * The learning goal's name.
   */
  name?: string;
}

/**
 * Interface for a displayable learning goal assignment.
 */
export interface IDisplayLearningGoalAssignmentModel extends ILearningGoalModel {
  /**
   * The optional root id
   */
  rootId?: string;
}

/**
 * Returns whether the given model is an instance of the
 * `IDisplayLearningGoalAssignmentModel` interface.
 *
 * @param model the learning goal model to test
 */
function instanceOfIDisplayLearningGoalAssignmentModel(model: ILearningGoalModel): model is IDisplayLearningGoalAssignmentModel {
  return 'rootId' in model;
}

/**
 * Converts the given {@link ILearningGoalModel} into the corresponding {@link LearningGoalTreeItem}.
 *
 * @param inputModel the model which should be converted
 */
export function convertLearningGoal(inputModel: ILearningGoalModel): LearningGoalTreeItem {
  let rootId = '';

  if (instanceOfIDisplayLearningGoalAssignmentModel(inputModel) && inputModel.rootId) {
    rootId = inputModel.rootId;
  }

  const retModel: LearningGoalTreeItem = {
    text: inputModel.name,
    description: inputModel.description,
    markedAsPrivate: inputModel.privateGoal,
    referencedFromCnt: inputModel.referencedFromCount,
    owner: inputModel.owner,
    changeDate: inputModel.lastModifiedDate,
    value: inputModel.id,
    children: [],
    rootId,
    needVerification: inputModel.needVerification,
  };

  for (const goal of inputModel.subGoals) {
    retModel.children!.push(convertLearningGoal(goal));
  }

  return retModel;
}
