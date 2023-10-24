export interface FDTaskSolve {
  id?: string,
  type?: string,
  solution?: string | null,
  closureSolutions?: Solve[]
  normalFormSolutions?: Solve[]
  maxPoints?: string | null
}

export interface Solve {
  id: string,
  solution?: string | null | undefined
}

export interface FDTaskSolveResponse {
  id: string
  solved: boolean
  hints?: FDHint[]
}

export interface FDHint {
  subId: string
  hint: string
}

