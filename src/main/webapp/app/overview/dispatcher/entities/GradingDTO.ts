/**
 * Maps information about a grading from the dispatcher
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
