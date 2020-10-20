/**
 * Interface for a new learning goal.
 */
import {LearningGoalTreeItem} from "./learning-goal-treeview-item.model";

export interface INewLearningGoalModel {
  /**
   * The name of the learning goal.
   */
  name: string,
  /**
   * The description of the learning goal.
   */
  description?: string,
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
   * The id of the learning goal.
   */
  id: string,
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
  subGoals: ILearningGoalModel[],
  /**
   * The count of lectures which hold a reference to this learning goal.
   */
  referencedFromCount: number
}

/**
 * Converts the given {@link ILearningGoalModel} into the corresponding {@link LearningGoalTreeItem}.
 *
 * @param inputModel the model which should be converted
 */
export function convertLearningGoal(inputModel: ILearningGoalModel): LearningGoalTreeItem {

  const retModel: LearningGoalTreeItem = {
    text: inputModel.name,
    description: inputModel.description,
    markedAsPrivate: inputModel.privateGoal,
    referencedFromCnt: inputModel.referencedFromCount,
    owner: inputModel.owner,
    changeDate: inputModel.lastModifiedDate,
    value: inputModel.id,
    children: []
  };

  for (let goal of inputModel.subGoals) {
    retModel.children!.push(convertLearningGoal(goal));
  }

  return retModel;
}
