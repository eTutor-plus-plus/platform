export interface FDModel {
  id: string;
  attributes: string[];
  functionalDependencies: FunctionalDependencies[];
  closures: Closure[];
  keys: Key[];
  minimalCovers: MinimalCover[] | null;
  normalForm: string;
  // bcnfDecompose: boolean;
}
export interface FunctionalDependencies {
  id: string;
  leftSide: string[];
  rightSide: string[];
  minimalCover: MinimalCover[],
  violates: string | null
}
export interface Closure {
  id: string;
  leftSide: string[];
  rightSide: string[];
}
export interface Key {
  id: string;
  leftSide: string[];
  rightSide: string[];
}
export interface MinimalCover {
  id: string;
  leftSide: string[];
  rightSide: string[];
  reasons: string
}
