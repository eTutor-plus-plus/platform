/**
 * The DTO that is required by the dispatcher to evaluate submissions
 */
export interface SubmissionDTO {
  submissionId: string;
  exerciseId: string;
  taskType: string;
  passedAttributes: any;
  passedParameters: Map<string, string>;
}
