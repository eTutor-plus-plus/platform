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
 * Interface which contains overview information
 * of student assignments.
 */
export interface IStudentAssignmentOverviewInfo {
  matriculationNo: string;
  submitted: boolean;
  fullyGraded: boolean;
}
