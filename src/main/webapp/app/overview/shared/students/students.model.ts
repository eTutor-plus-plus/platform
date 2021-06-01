/**
 * Interface which represents a student
 */
export interface IStudentInfoDTO {
  /**
   * The student's first name.
   */
  firstName: string;
  /**
   * The student's last name.
   */
  lastName: string;
  /**
   * The student's unique matriculation number.
   */
  matriculationNumber: string;
}

/**
 * Interface which extends the regular `IStudentInfoDTO` interface by adding
 * a full name property for the ng-select control.
 */
export interface IStudentFullNameInfoDTO extends IStudentInfoDTO {
  /**
   * The full name.
   */
  fullName: string;
}

/**
 * Interface which represents a course instance.
 */
export interface ICourseInstanceInformationDTO {
  /**
   * The course name.
   */
  courseName: string;
  /**
   * The internal term id.
   */
  termId: string;
  /**
   * The course instructor.
   */
  instructor: string;
  /**
   * The internal instance id.
   */
  instanceId: string;
  /**
   * The year.
   */
  year: number;
  /**
   * Indicates whether the student has already
   * completed the initial self assessment or not.
   */
  initialSelfAssessmentCompleted: boolean;
}

/**
 * Interface which represents the progress overview of a course instance.
 */
export interface ICourseInstanceProgressOverviewDTO {
  /**
   * The internal exercise sheet id.
   */
  exerciseSheetId: string;
  /**
   * The assignment header.
   */
  assignmentHeader: string;
  /**
   * The difficulty URI.
   */
  difficultyURI: string;
  /**
   * The status of the assignment.
   */
  completed: boolean;
  /**
   * Indicates whether the exercise sheet has already been opened or not.
   */
  opened: boolean;
  /**
   * The actual task count.
   */
  actualCount: number;
  /**
   * The submitted task count.
   */
  submissionCount: number;
  /**
   * The graded task count.
   */
  gradedCount: number;
  /**
   * Indicates whether the exercise sheet has already been closed or not
   */
  closed: boolean;
}

/**
 * Interface which represents
 */
export interface IStudentTaskListInfoDTO {
  /**
   * The order number.
   */
  orderNo: number;
  /**
   * The internal task id.
   */
  taskId: string;
  /**
   * Indicates whether the task is graded or not.
   */
  graded: boolean;
  /**
   * Indicates whether the learning goal is reached or not
   * only used when the task has already been graded.
   */
  goalCompleted: boolean;
  /**
   * The task's header.
   */
  taskHeader: string;
  /**
   * Indicates whether the task has already been submitted or not.
   */
  submitted: boolean;
}
