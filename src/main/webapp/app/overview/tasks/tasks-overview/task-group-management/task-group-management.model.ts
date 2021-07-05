/**
 * Interface which represents a new task group
 */
export interface INewTaskGroupDTO {
  /**
   * The mandatory name.
   */
  name: string;
  /**
   * The optional description
   */
  description?: string;
}

/**
 * Interface which represents a task group entry.
 */
export interface ITaskGroupDTO extends INewTaskGroupDTO {
  /**
   * The internal id.
   */
  id: string;
  /**
   * The creator's login.
   */
  creator: string;
  /**
   * The change date
   */
  changeDate: Date;
}

/**
 * Interface for displaying task groups.
 */
export interface ITaskGroupDisplayDTO {
  /**
   * The internal id.
   */
  id: string;
  /**
   * The name.
   */
  name: string;
}