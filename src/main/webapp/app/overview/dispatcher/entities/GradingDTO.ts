/**
 * The Grading-DTO to map grading responses from the dispatcher
 */
export interface GradingDTO {
  submissionId: string;
  maxPoints: number;
  points: number;
  result: string;
  report: {
    description: string;
    error: string;
    hint: string;
  };
}
