export interface SubmissionDTO {
  submissionId: string;
  exerciseId: string;
  taskType: string;
  passedAttributes: any;
  passedParameters: Map<string, string>;
}
