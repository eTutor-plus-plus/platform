export interface SubmissionEvent {
  submission: string;
  isSubmitted: boolean;
  hasBeenSolved: boolean;
  dispatcherId: number;
  taskType: string;
}
