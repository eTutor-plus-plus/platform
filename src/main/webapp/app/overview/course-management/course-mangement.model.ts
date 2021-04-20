import { IStudentInfoDTO } from '../shared/students/students.model';

/**
 * Interface for a course.
 */
export interface ICourseModel {
  /**
   * The optional course id.
   */
  id?: string;
  /**
   * The mandatory course name.
   */
  name: string;
  /**
   * The optional course description.
   */
  description?: string;
  /**
   * The optional course link.
   */
  link?: URL;
  /**
   * The mandatory course type.
   */
  courseType: string;
  /**
   * The optional creator of this course.
   */
  creator?: string;
  /**
   * The instance count.
   */
  instanceCount: number;
}

/**
 * Class which implements the `ICourseModel` interface.
 */
export class CourseModel implements ICourseModel {
  /**
   * Constructor.
   *
   * @param name the mandatory course name
   * @param courseType the mandatory course type
   * @param instanceCount the instance count
   * @param id the optional id
   * @param description the optional description
   * @param link the optional link
   * @param creator the optional creator
   */
  constructor(
    public name: string,
    public courseType: string,
    public instanceCount: number,
    public id?: string,
    public description?: string,
    public link?: URL,
    public creator?: string
  ) {}
}

/**
 * Interface for learning goal assignment updates.
 */
export interface ILearningGoalUpdateAssignment {
  courseId: string;
  learningGoalIds: string[];
}

/**
 * Represents a term.
 */
export class Term {
  public static readonly Summer = new Term('http://www.dke.uni-linz.ac.at/etutorpp/Term#Summer', 'courseManagement.terms.summer');
  public static readonly Winter = new Term('http://www.dke.uni-linz.ac.at/etutorpp/Term#Winter', 'courseManagement.terms.winter');

  public static readonly Values = [Term.Winter, Term.Summer];

  private readonly _value: string;
  private readonly _text: string;

  /**
   * Constructor.
   *
   * @param value the term url
   * @param text the term display text
   */
  constructor(value: string, text: string) {
    this._value = value;
    this._text = text;
  }

  /**
   * Returns the term from the given url.
   *
   * @param url the url
   */
  public static fromString(url: string): Term | undefined {
    return Term.Values.find(x => x.value === url);
  }

  /**
   * Returns the value
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
   * Overrides the to string method.
   */
  public toString = (): string => this.text;
}

/**
 * Interface which represents a new course instance.
 */
export interface INewCourseInstanceDTO {
  /**
   * The id of the corresponding course.
   */
  courseId: string;
  /**
   * The year of the course holding.
   */
  year: number;
  /**
   * The id of the corresponding term.
   */
  termId: string;
  /**
   * The optional description.
   */
  description?: string;
}

/**
 * Interface which represents a course instance.
 */
export interface ICourseInstanceDTO {
  /**
   * The year of the course holding.
   */
  year: number;
  /**
   * The id of the course's term.
   */
  termId: string;
  /**
   * The optional description.
   */
  description?: string;
  /**
   * The internal course instance id.
   */
  id: string;
  /**
   * The list of students.
   */
  students: IStudentInfoDTO[];
  /**
   * The name of the course.
   */
  courseName: string;
  /**
   * The course instance name.
   */
  instanceName: string;
}

/**
 * Interface which represents a displayable
 * course instance overview entry.
 */
export interface IDisplayableCourseInstanceDTO {
  /**
   * The internal course instance id.
   */
  id: string;
  /**
   * The instance name.
   */
  name: string;
  /**
   * The count of assigned students.
   */
  studentCount: number;
  /**
   * The year of the holding.
   */
  year: number;
  /**
   * The term id url
   */
  termId: string;
}
