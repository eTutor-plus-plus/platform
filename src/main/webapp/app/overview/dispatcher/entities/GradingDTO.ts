/**
 * Maps information about a grading from the dispatcher
 */
export interface GradingDTO {
  submissionId: string;
  maxPoints: number;
  points: number;
  result: string;
  submissionSuitsSolution: boolean;
  report: {
    description: string;
    error: string;
    hint: string;
  };
}
