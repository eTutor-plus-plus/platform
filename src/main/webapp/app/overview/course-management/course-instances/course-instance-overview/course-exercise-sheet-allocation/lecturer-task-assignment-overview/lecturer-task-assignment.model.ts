/**
 * Interface which is used to contain the information of an assigned exercise sheet.
 */
export interface ILecturerTaskAssignmentInfoModel {
  /**
   * The course instance id.
   */
  courseInstanceId: string;
  /**
   * The exercise sheet id.
   */
  exerciseSheetId: string;
}

/**
 * Interface which extends the `ILecturerTaskAssignmentInfoModel`-interface by adding the matriculation number.
 */
export interface ILecturerStudentTaskAssignmentInfoModel extends ILecturerTaskAssignmentInfoModel {
  /**
   * The student's matriculation number.
   */
  matriculationNo: string;
}

/**
 * Interface which contains overview information
 * of student assignments.
 */
export interface IStudentAssignmentOverviewInfo {
  /**
   * The matriculation number.
   */
  matriculationNo: string;
  /**
   * Indicates whether the assignment has been submitted or not.
   */
  submitted: boolean;
  /**
   * Indicates whether the assignment is fully graded or not.
   */
  fullyGraded: boolean;
  /**
   * The expected task count.
   */
  expectedTaskCount: number;
  /**
   * The currently submitted tasks' count.
   */
  currentTaskCount: number;
}

/**
 * Interface which contains the grading info
 * for a lecturer.
 */
export interface ILecturerGradingInfo {
  /**
   * The internal task url.
   */
  taskURL: string;
  /**
   * The task title
   */
  taskTitle: string;
  /**
   * Indicates whether the task's goals have been completed or not.
   */
  completed: boolean;
  /**
   * Indicates whether the task has already been graded or not.
   */
  graded: boolean;
  /**
   * The internal order number.
   */
  orderNo: number;
  /**
   * Indicates whether the assignment has already been submitted.
   */
  submitted: boolean;
  /**
   * The task type's id.
   */
  taskTypeId: string;
}

/**
 * Interface which represents the grading info view model.
 */
export interface IGradingInfoVM {
  /**
   * The course instance uuid.
   */
  courseInstanceUUID: string;
  /**
   * The exercise sheet uuid.
   */
  exerciseSheetUUID: string;
  /**
   * The matriculation number.
   */
  matriculationNo: string;
  /**
   * The order number of the task.
   */
  orderNo: number;
  /**
   * The goal completion status.
   */
  goalCompleted: boolean;
}
