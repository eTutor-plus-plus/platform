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
