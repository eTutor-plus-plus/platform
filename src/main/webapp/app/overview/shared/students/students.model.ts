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
}
