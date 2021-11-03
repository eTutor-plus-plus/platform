export interface TaskPointEntryModel {
  /**
   * The student´s matriculation number
   */
  matriculationNo: string;
  /**
   * The maximum points of the task assignment
   */
  maxPoints: number;
  /**
   * The achieved points of the individual task
   */
  points: number;
  /**
   * The task assignment´s header
   */
  taskHeader: string;
  /**
   * The task assignment´s UUID
   */
  taskUUID: string;
}
