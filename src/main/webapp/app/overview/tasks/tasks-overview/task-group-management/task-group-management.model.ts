/**
 * Interface which represents a new task group
 */
export interface INewTaskGroupDTO {
  /**
   * The mandatory name.
   */
  name: string;
  /**
   * The optional description
   */
  description?: string;
  /**
   * The id of the task group's type
   */
  taskGroupTypeId: string;
  /**
   * Optional create-table-statements for an SQL-Task Group
   */
  sqlCreateStatements?: string;
  /**
   * Optional insert-into-statements for an SQL-Task Group for submissions
   */
  sqlInsertStatementsSubmission?: string;
  /**
   * Optional insert-into-statements for an SQL-Task Group for diagnose
   */
  sqlInsertStatementsDiagnose?: string;
  /**
   * Optional XML document for an XQuery task group(diagnose-version)
   */
  xQueryDiagnoseXML?: string;
  /**
   * Optional XML document for an XQuery task group(submit-version)
   */
  xQuerySubmissionXML?: string;
  /**
   * Optional facts for a Datalog task group
   */
  datalogFacts?: string;

  /**
   * Optional file url for a task group
   */
  fileUrl?: string;
}

/**
 * Interface which represents a task group entry.
 */
export interface ITaskGroupDTO extends INewTaskGroupDTO {
  /**
   * The internal id.
   */
  id: string;
  /**
   * The creator's login.
   */
  creator: string;
  /**
   * The change date
   */
  changeDate: Date;
}

/**
 * Interface for displaying task groups.
 */
export interface ITaskGroupDisplayDTO {
  /**
   * The internal id.
   */
  id: string;
  /**
   * The name.
   */
  name: string;
}

/**
 * Represents a task group type
 */
export class TaskGroupType {
  public static readonly NoType = new TaskGroupType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskGroupType#NoType',
    'taskManagement.taskGroup.update.types.noType' // add translation!
  );

  public static readonly SQLType = new TaskGroupType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskGroupType#SQLType',
    'taskManagement.taskGroup.update.types.sqlType'
  );

  public static readonly XQueryType = new TaskGroupType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskGroupType#XQueryType',
    'taskManagement.taskGroup.update.types.xQueryType'
  );

  public static readonly DatalogType = new TaskGroupType(
    'http://www.dke.uni-linz.ac.at/etutorpp/TaskGroupType#DatalogType',
    'taskManagement.taskGroup.update.types.datalogType'
  );

  public static readonly Values = [TaskGroupType.NoType, TaskGroupType.SQLType, TaskGroupType.XQueryType, TaskGroupType.DatalogType];

  private readonly _value: string;
  private readonly _text: string;

  /**
   * Constructr
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
