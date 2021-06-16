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
