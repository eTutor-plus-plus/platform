/**
 * Wraps information that is persisted for every submission for a task assignment
 */
export interface SubmissionEvent {
  submission: string;
  isSubmitted: boolean;
  hasBeenSolved: boolean;
  dispatcherId: number;
  taskType: string;
}
