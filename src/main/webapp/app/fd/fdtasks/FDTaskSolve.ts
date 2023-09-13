export interface FDTaskSolve {
  id?: string,
  type?: string,
  solution?: string | null,
  closureSolutions?: Solve[]
  normalFormSolutions?: Solve[]
}

export interface Solve {
  id: string,
  solution?: string | null | undefined
}

export interface FDTaskSolveResponse {
  id: string
  solved: boolean
  hint?: FDHint[]
}

export interface FDHint {
  subId: string
  hint: string
}

