export interface FdModel {
  id: string;
  attributes: string[];
  fDependencies: Dependency[];
  closures: Dependency[];
  keys: Key[];
  minimalCovers: MinimalCover[];
  normalForm: string;
  // bcnfDecompose: boolean;
}
export interface Dependency {
  id: string;
  leftSide: string[];
  rightSide: string[];
}
export interface Key {
  id: string;
  value: string[];
}
export interface MinimalCover {
  cover: Dependency;
  reason: string;
  dependency: Dependency;
}
