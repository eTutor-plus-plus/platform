import { ILearningGoalDisplayModel } from '../shared/learning-goal-model';

/**
 * Interface for a task display model.
 */
export interface ITaskDisplayModel {
  /**
   * The task's internal id url.
   */
  taskId: string;
  /**
   * The task's header.
   */
  header: string;
  /**
   * The task's internal creator.
   */
  internalCreator: string;
  /**
   * Indicates whether this task is a private task or not.
   */
  privateTask: boolean;
}

/**
 * Interface which represents a new task.
 */
export interface INewTaskModel {
  /**
   * The header of the task.
   */
  header: string;
  /**
   * The creator of the task.
   */
  creator: string;
  /**
   * The organisation unit of the task
   */
  organisationUnit: string;
  /**
   * Indicates whether the given task should be a private task or public one.
   */
  privateTask: boolean;
  /**
   * The id of the task's difficulty.
   */
  taskDifficultyId: string;
  /**
   * The optional free text processing time.
   */
  processingTime?: string;
  /**
   * The optional assignment url.
   */
  url?: URL;
  /**
   * The optional instruction text.
   */
  instruction?: string;
  /**
   * The associated learning goals.
   */
  learningGoalIds: ILearningGoalDisplayModel[];
  /**
   * The id of the task's type.
   **/
  taskAssignmentTypeId: string;
}

/**
 * Interface which extends the new task model interface
 * by adding a task's id and creation date.
 */
export interface ITaskModel extends INewTaskModel {
  /**
   * The task's internal id.
   */
  id: string;
  /**
   * The task's creation date.
   */
  creationDate: Date;
  /**
   * The task's internal creator.
   */
  internalCreator: string;
}

/**
 * Returns whether the given model is an instance of the
 * `ITaskModel` interface or not.
 *
 * @param model the new task model to test
 */
export function instanceOfITaskModel(model: INewTaskModel): model is ITaskModel {
  return 'id' in model;
}

/**
 * Represents a task assignment type.
 */
export class TaskAssignmentType {
  public static readonly NoType = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NoType',
    'taskManagement.taskTypes.noType'
  );
  public static readonly UploadTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#UploadTask',
    'taskManagement.taskTypes.uploadTask'
  );
  public static readonly SQLTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#SQLTask',
    'taskManagement.taskTypes.sqlTask'
  );

  public static readonly Values = [TaskAssignmentType.NoType, TaskAssignmentType.UploadTask, TaskAssignmentType.SQLTask];

  private readonly _value: string;
  private readonly _text: string;

  /**
   * Constructor.
   *
   * @param value the value
   * @param text the text
   */
  constructor(value: string, text: string) {
    this._value = value;
    this._text = text;
  }

  /**
   * Returns the value.
   */
  public get value(): string {
    return this._value;
  }

  /**
   * Returns the text.
   */
  public get text(): string {
    return this._text;
  }

  /**
   * Overridden toString method.
   */
  public toString = (): string => this._text;
}

/**
 * Represents a task difficulty.
 */
export class TaskDifficulty {
  public static readonly Easy = new TaskDifficulty(
    'http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#Easy',
    'taskManagement.difficulties.easy'
  );
  public static readonly Medium = new TaskDifficulty(
    'http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#Medium',
    'taskManagement.difficulties.medium'
  );
  public static readonly Hard = new TaskDifficulty(
    'http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#Hard',
    'taskManagement.difficulties.hard'
  );
  public static readonly VeryHard = new TaskDifficulty(
    'http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#VeryHard',
    'taskManagement.difficulties.veryHard'
  );

  public static readonly Values = [TaskDifficulty.Easy, TaskDifficulty.Medium, TaskDifficulty.Hard, TaskDifficulty.VeryHard];

  private readonly _value: string;
  private readonly _text: string;

  /**
   * Constructor.
   *
   * @param value the difficulty url
   * @param text the display text
   */
  constructor(value: string, text: string) {
    this._value = value;
    this._text = text;
  }

  /**
   * Returns the task difficulty from the given string.
   *
   * @param url the url
   */
  public static fromString(url: string): TaskDifficulty | undefined {
    return TaskDifficulty.Values.find(x => x.value === url);
  }

  /**
   * Returns the value.
   */
  public get value(): string {
    return this._value;
  }

  /**
   * Returns the text.
   */
  public get text(): string {
    return this._text;
  }

  /**
   * Overrides the toString method.
   */
  public toString = (): string => this.text;
}

/**
 * Interface for displaying a task assignment.
 */
export interface ITaskAssignmentDisplay {
  /**
   * The task assignment's header.
   */
  header: string;
  /**
   * The task assignment's internal id.
   */
  id: string;
}
