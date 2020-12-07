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
   * @param id the optional id
   * @param description the optional description
   * @param link the optional link
   * @param creator the optional creator
   */
  constructor(
    public name: string,
    public courseType: string,
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
