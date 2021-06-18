export interface SubmissionDTO {
  submissionId: string;
  exerciseId: number;
  taskType: string;
  passedAttributes: any;
  passedParameters: Map<string, string>;
}
