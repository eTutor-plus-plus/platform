import { ILearningGoalDisplayModel } from '../shared/learning-goal-model';

/**
 * Interface for a task display model.
 */
export interface ITaskDisplayModel {
  /**
   * The task's internal id url.
   */
  taskId: string;
  /**
   * The task's header.
   */
  header: string;
  /**
   * The task's internal creator.
   */
  internalCreator: string;
  /**
   * Indicates whether this task is a private task or not.
   */
  privateTask: boolean;
}

/**
 * Interface which represents a new task.
 */
export interface INewTaskModel {
  /**
   * The header of the task.
   */
  header: string;
  /**
   * The creator of the task.
   */
  creator: string;
  /**
   * The organisation unit of the task
   */
  organisationUnit: string;
  /**
   * Indicates whether the given task should be a private task or public one.
   */
  privateTask: boolean;
  /**
   * The id of the task's difficulty.
   */
  taskDifficultyId: string;
  /**
   * The optional id of an uploaded file representing the solution or addition information for students
   */
  uploadFileId?: number;

  writerInstructionFileId?: number;

  calcSolutionFileId?: number;

  calcInstructionFileId?: number;

  startTime?: string;

  endTime?: string;

  //TODO: starttime und endtime
  /**
   * The optional  task id for the dispatcher.
   */
  taskIdForDispatcher?: string;
  /**
   * The optional weighting with regards to the diagnose level
   */
  diagnoseLevelWeighting?: string;
  /**
   * Optional solution for a SQL-assignment
   */
  sqlSolution?: string;
  /**
   * Optional solution for a XQuery-assignment
   */
  xQuerySolution?: string;
  /**
   * Optional XPath-expression defining the sorting of an XQuery-assignment
   */
  xQueryXPathSorting?: string;
  /**
   * Optional solution for a datalog task
   */
  datalogSolution?: string;
  /**
   * Optional query for a datalog task
   */
  datalogQuery?: string;
  /**
   * Optional unchecked terms for a datalog task
   */
  datalogUncheckedTerms?: string;
  /**
   * Optional max points
   */
  maxPoints?: string;
  /**
   * The optional free text processing time.
   */
  processingTime?: string;
  /**
   * The optional assignment url.
   */
  url?: URL;
  /**
   * The optional instruction text.
   */
  instruction?: string;
  /**
   * The associated learning goals.
   */
  learningGoalIds: ILearningGoalDisplayModel[];
  /**
   * The id of the task's type.
   **/
  taskAssignmentTypeId: string;
  /**
   * The optional task group id.
   */
  taskGroupId?: string;
  /**
   * The optional Bpmn Testconfig
   */
  bpmnTestConfig?: string;

  /**
   * PM Task related configuration values
   * the maximal number of activities in a trace
   */
  maxActivity?: number;
  /**
   * PM Task related configuration values
   * the minimal number of activities in a trace
   */
  minActivity?: number;
  /**
   * PM Task related configuration values
   * the maximal log size
   */
  maxLogSize?: number;
  /**
   * PM Task related configuration values
   * the minimal log size
   */
  minLogSize?: number;
  /**
   * PM Task related configuration values
   * the configuration number (either config1, config2, config3, default)
   */
  configNum?: string;

  /** apriori start */
  /**
   *	Optional dataset id for apriori
   */
  aprioriDatasetId?: string;

  /** apriori end */

  // NF start

  /**
   * NF-specific variable: name of base relation
   */
  nfBaseRelationName?: string;

  /**
   * NF-specific variable: attributes of base relation
   */
  nfBaseAttributes?: string;

  /**
   * NF-specific variable: functional dependencies of base relation
   */
  nfBaseDependencies?: string;

  /**
   * NF-specific variable: the id of the NF task subtype
   */
  nfTaskSubtypeId?: string;

  // Keys determination

  /**
   * NF-specific variable: Number of points deducted per missing key in a keys determination task
   */
  nfKeysDeterminationPenaltyPerMissingKey?: number;

  /**
   * NF-specific variable: Number of points deducted per incorrect key in a keys determination task
   */
  nfKeysDeterminationPenaltyPerIncorrectKey?: number;

  // Attribute closure

  /**
   * NF-specific variable: base attributes for closure in an attribute closure task
   */
  nfAttributeClosureBaseAttributes?: string;

  /**
   * NF-specific variable: Number of points deducted per missing attribute an attribute closure task
   */
  nfAttributeClosurePenaltyPerMissingAttribute?: number;

  /**
   * NF-specific variable: Number of points deducted per incorrect attribute in an attribute closure task
   */
  nfAttributeClosurePenaltyPerIncorrectAttribute?: number;

  // Minimal cover

  /**
   * NF-specific variable: Number of points deducted per non-canonical functional dependency in a minimal cover task
   */
  nfMinimalCoverPenaltyPerNonCanonicalDependency?: number;

  /**
   * NF-specific variable: Number of points deducted per trivial functional dependency in a minimal cover task
   */
  nfMinimalCoverPenaltyPerTrivialDependency?: number;

  /**
   * NF-specific variable: Number of points deducted per extraneous attribute on the left-hand side of a functional
   * dependency in a minimal cover task
   */
  nfMinimalCoverPenaltyPerExtraneousAttribute?: number;

  /**
   * NF-specific variable: Number of points deducted per redundant functional dependency in a minimal cover task
   */
  nfMinimalCoverPenaltyPerRedundantDependency?: number;

  /**
   * NF-specific variable: Number of points deducted per missing functional dependency in a minimal cover task
   * (compared to the correct solution)
   */
  nfMinimalCoverPenaltyPerMissingDependencyVsSolution?: number;

  /**
   * NF-specific variable: Number of points deducted per incorrect functional dependency in a minimal cover task
   * (compared to the correct solution)
   */
  nfMinimalCoverPenaltyPerIncorrectDependencyVsSolution?: number;

  // Normal form determination

  /**
   * NF-specific variable: Number of points deducted for an incorrect total normal form in a normal form determination
   * task
   */
  nfNormalFormDeterminationPenaltyForIncorrectOverallNormalform?: number;

  /**
   * NF-specific variable: Number of points deducted per incorrectly determined normal form of a functional dependency
   * in a normal form determination task
   */
  nfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform?: number;

  // Normalization

  /**
   * NF-specific variable: The minimum normal form level which the resulting relations must have in a normalization
   * task
   */
  nfNormalizationTargetLevel?: string;

  /**
   * NF-specific variable: The maximum number of functional dependencies that is permitted to be lost in the
   * decomposition process before points are deducted in a normalization task
   */
  nfNormalizationMaxLostDependencies?: number;

  /**
   * NF-specific variable: Points deducted for every attribute of the base relation that is not present in any of the
   * resulting relations in a normalization task
   */
  nfNormalizationPenaltyPerLostAttribute?: number;

  /**
   * NF-specific variable: Points deducted if the resulting relations cannot be re-combined into the base relation in
   * a normalization task
   */
  nfNormalizationPenaltyForLossyDecomposition?: number;

  /**
   * NF-specific variable: Points deducted for every non-canonical functional dependency in a resulting relation in
   * a normalization task
   */
  nfNormalizationPenaltyPerNonCanonicalDependency?: number;

  /**
   * NF-specific variable: Points deducted for every trivial functional dependency in a resulting relation in
   * a normalization task
   */
  nfNormalizationPenaltyPerTrivialDependency?: number;

  /**
   * NF-specific variable: Points deducted for every extraneous attribute on the left-hand side of a functional
   * dependency in a resulting relation in a normalization task
   */
  nfNormalizationPenaltyPerExtraneousAttributeInDependencies?: number;

  /**
   * NF-specific variable: Points deducted for every redundant functional dependency in a resulting relation in
   * a normalization task
   */
  nfNormalizationPenaltyPerRedundantDependency?: number;

  /**
   * NF-specific variable: Points deducted for every functional dependency that was lost during the decomposition
   * process and exceeds the maximum permitted number of lost functional dependencies in a normalization task
   */
  nfNormalizationPenaltyPerExcessiveLostDependency?: number;

  /**
   * NF-specific variable: Points deducted for every functional dependency that would have to exist in a resulting
   * relation due to the decomposition process but does not in a normalization task
   */
  nfNormalizationPenaltyPerMissingNewDependency?: number;

  /**
   * NF-specific variable: Points deducted for every functional dependency that exists in a resulting relation, even
   * though it is not supposed to (due to the decomposition process, more specifically the RBR algorithm) in a
   * normalization task
   */
  nfNormalizationPenaltyPerIncorrectNewDependency?: number;

  /**
   * NF-specific variable: Points deducted for every missing key in a resulting relation in a normalization task
   */
  nfNormalizationPenaltyPerMissingKey?: number;

  /**
   * NF-specific variable: Points deducted for every incorrect key in a resulting relation in a normalization task
   */
  nfNormalizationPenaltyPerIncorrectKey?: number;

  /**
   * NF-specific variable: Points deducted for every resulting relation that does not match or exceed the required
   * normal form in a normalization task
   */
  nfNormalizationPenaltyPerIncorrectNFRelation?: number;
  // NF end
}

/**
 * Interface which extends the new task model interface
 * by adding a task's id and creation date.
 * //PMTask Model
 */
export interface ITaskModel extends INewTaskModel {
  /**
   * The task's internal id.
   */
  id: string;
  /**
   * The task's creation date.
   */
  creationDate: Date;
  /**
   * The task's internal creator.
   */
  internalCreator: string;
}

/**
 * Returns whether the given model is an instance of the
 * `ITaskModel` interface or not.
 *
 * @param model the new task model to test
 */
export function instanceOfITaskModel(model: INewTaskModel): model is ITaskModel {
  return 'id' in model;
}

/**
 * Represents a task assignment type.
 */
export class TaskAssignmentType {
  public static readonly NoType = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NoType',
    'taskManagement.taskTypes.noType'
  );
  public static readonly UploadTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#UploadTask',
    'taskManagement.taskTypes.uploadTask'
  );
  public static readonly SQLTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#SQLTask',
    'taskManagement.taskTypes.sqlTask'
  );

  public static readonly RATask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#RATask',
    'taskManagement.taskTypes.raTask'
  );

  public static readonly XQueryTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#XQTask',
    'taskManagement.taskTypes.xqTask'
  );

  public static readonly DatalogTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#DLGTask',
    'taskManagement.taskTypes.dlgTask'
  );
  public static readonly BpmnTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#BpmnTask',
    'taskManagement.taskTypes.bpmnTask'
  );
  public static readonly PmTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#PmTask',
    'taskManagement.taskTypes.pmTask'
  );

  public static readonly CalcTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#CalcTask',
    'taskManagement.taskTypes.calcTask'
  );
  public static readonly AprioriTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#AprioriTask',
    'taskManagement.taskTypes.aprioriTask'
  );

  public static readonly UmlTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#UMLTask',
    'taskManagement.taskTypes.umlTask'
  );

  public static readonly NfTask = new TaskAssignmentType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask',
    'taskManagement.taskTypes.nfTask'
  );

  public static readonly Values = [
    TaskAssignmentType.NoType,
    TaskAssignmentType.UploadTask,
    TaskAssignmentType.SQLTask,
    TaskAssignmentType.RATask,
    TaskAssignmentType.XQueryTask,
    TaskAssignmentType.DatalogTask,
    TaskAssignmentType.CalcTask,
    TaskAssignmentType.BpmnTask,
    TaskAssignmentType.PmTask,
    TaskAssignmentType.AprioriTask,
    TaskAssignmentType.UmlTask,
    TaskAssignmentType.NfTask,
  ];

  private readonly _value: string;
  private readonly _text: string;

  /**
   * Constructor.
   *
   * @param value the value
   * @param text the text
   */
  constructor(value: string, text: string) {
    this._value = value;
    this._text = text;
  }

  /**
   * Returns the value.
   */
  public get value(): string {
    return this._value;
  }

  /**
   * Returns the text.
   */
  public get text(): string {
    return this._text;
  }

  /**
   * Overridden toString method.
   */
  public toString = (): string => this._text;
}

/**
 * Represents a task difficulty.
 */
export class TaskDifficulty {
  public static readonly Easy = new TaskDifficulty(
    'http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#Easy',
    'taskManagement.difficulties.easy'
  );
  public static readonly Medium = new TaskDifficulty(
    'http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#Medium',
    'taskManagement.difficulties.medium'
  );
  public static readonly Hard = new TaskDifficulty(
    'http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#Hard',
    'taskManagement.difficulties.hard'
  );
  public static readonly VeryHard = new TaskDifficulty(
    'http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#VeryHard',
    'taskManagement.difficulties.veryHard'
  );

  public static readonly Values = [TaskDifficulty.Easy, TaskDifficulty.Medium, TaskDifficulty.Hard, TaskDifficulty.VeryHard];

  private readonly _value: string;
  private readonly _text: string;

  /**
   * Constructor.
   *
   * @param value the difficulty url
   * @param text the display text
   */
  constructor(value: string, text: string) {
    this._value = value;
    this._text = text;
  }

  /**
   * Returns the task difficulty from the given string.
   *
   * @param url the url
   */
  public static fromString(url: string): TaskDifficulty | undefined {
    return TaskDifficulty.Values.find(x => x.value === url);
  }

  /**
   * Returns the value.
   */
  public get value(): string {
    return this._value;
  }

  /**
   * Returns the text.
   */
  public get text(): string {
    return this._text;
  }

  /**
   * Overrides the toString method.
   */
  public toString = (): string => this.text;
}

/**
 * Interface for displaying a task assignment.
 */
export interface ITaskAssignmentDisplay {
  /**
   * The task assignment's header.
   */
  header: string;
  /**
   * The task assignment's internal id.
   */
  id: string;
}

// NF start
/**
 * Represents an NF task's subtype.
 */
export class NFTaskSubtype {
  public static readonly KeysDeterminationTask = new NFTaskSubtype(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#KeysDeterminationTask',
    'taskManagement.taskTypes.nfTask.keysDeterminationTask'
  );

  public static readonly AttributeClosureTask = new NFTaskSubtype(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#AttributeClosureTask',
    'taskManagement.taskTypes.nfTask.attributeClosureTask'
  );

  public static readonly MinimalCoverTask = new NFTaskSubtype(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#MinimalCoverTask',
    'taskManagement.taskTypes.nfTask.minimalCoverTask'
  );

  public static readonly NormalformDeterminationTask = new NFTaskSubtype(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#NormalformDeterminationTask',
    'taskManagement.taskTypes.nfTask.normalformDeterminationTask'
  );

  public static readonly NormalizationTask = new NFTaskSubtype(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#NFTask#NormalizationTask',
    'taskManagement.taskTypes.nfTask.normalizationTask'
  );

  public static readonly Values = [
    NFTaskSubtype.KeysDeterminationTask,
    NFTaskSubtype.AttributeClosureTask,
    NFTaskSubtype.MinimalCoverTask,
    NFTaskSubtype.NormalformDeterminationTask,
    NFTaskSubtype.NormalizationTask,
  ];

  private readonly _value: string;
  private readonly _text: string;

  /**
   * Constructor.
   *
   * @param value the value
   * @param text the text
   */
  constructor(value: string, text: string) {
    this._value = value;
    this._text = text;
  }

  /**
   * Returns the value.
   */
  public get value(): string {
    return this._value;
  }

  /**
   * Returns the text.
   */
  public get text(): string {
    return this._text;
  }

  /**
   * Overridden toString method.
   */
  public toString = (): string => this._text;
}
// NF end
