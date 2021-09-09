export interface TaskSubmissionsModel {
  hasBeenSolved: boolean;
  isSubmitted: boolean;
  dispatcherId: number;
  instant: string;
  submission: string;
  taskType: string;
}
