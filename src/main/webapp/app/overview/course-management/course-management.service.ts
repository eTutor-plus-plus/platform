import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CourseModel,
  ICourseInstanceDTO,
  IDisplayableCourseInstanceDTO,
  ILearningGoalUpdateAssignment,
  INewCourseInstanceDTO,
} from './course-mangement.model';
import { SERVER_API_URL } from '../../app.constants';
import { LearningGoalTreeviewItem } from '../shared/learning-goal-treeview-item.model';
import { convertLearningGoal, IDisplayLearningGoalAssignmentModel } from '../shared/learning-goal-model';
import { map } from 'rxjs/operators';
import { createRequestOption, Pagination } from '../../shared/util/request-util';
import { IStudentInfoDTO } from '../shared/students/students.model';

/**
 * Service which manages the courses.
 */
@Injectable({
  providedIn: 'root',
})
export class CourseManagementService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Returns all courses.
   *
   * @returns an observable which contains a list of {@link CourseModel}
   */
  public getAllCourses(): Observable<CourseModel[]> {
    return this.http.get<CourseModel[]>(SERVER_API_URL + 'api/course');
  }

  /**
   * Posts the given course to the rest endpoint.
   *
   * @param course the course to post
   * @returns an observable which contains the created {@link CourseModel}
   */
  public postCourse(course: CourseModel): Observable<CourseModel> {
    return this.http.post<CourseModel>(SERVER_API_URL + 'api/course', course);
  }

  /**
   * Updates the given course.
   *
   * @param course the course which should be updated
   * @returns the observable of the request
   */
  public putCourse(course: CourseModel): Observable<any> {
    return this.http.put(SERVER_API_URL + 'api/course', course);
  }

  /**
   * Deletes the given course.
   *
   * @param course the course to delete
   */
  public deleteCourse(course: CourseModel): Observable<any> {
    return this.http.delete(SERVER_API_URL + `api/course/${course.name}`);
  }

  /**
   * Returns the learning goals of the given course.
   *
   * @param course the course model
   * @param userLogin the current user's login name
   */
  public getLearningGoalsFromCourse(course: CourseModel, userLogin: string): Observable<LearningGoalTreeviewItem[]> {
    return this.http.get<IDisplayLearningGoalAssignmentModel[]>(SERVER_API_URL + `api/course/${course.name}/goals`).pipe(
      map(list => {
        const retList: LearningGoalTreeviewItem[] = [];

        for (const item of list) {
          retList.push(new LearningGoalTreeviewItem(convertLearningGoal(item), userLogin));
        }

        return retList;
      })
    );
  }

  /**
   * Sets the learning goal assignment.
   *
   * @param learningGoalAssignment the assignment to set
   */
  public setLearningGoalAssignment(learningGoalAssignment: ILearningGoalUpdateAssignment): Observable<any> {
    return this.http.put(SERVER_API_URL + 'api/course/goal', learningGoalAssignment);
  }

  /**
   * Creates a new course instance.
   *
   * @param newInstance the instance dto
   */
  public createInstance(newInstance: INewCourseInstanceDTO): Observable<HttpResponse<string>> {
    return this.http.post<string>(SERVER_API_URL + 'api/course-instance', newInstance, { observe: 'response' });
  }

  /**
   * Returns the instances of the given course name.
   *
   * @param courseName the course's name
   */
  public getInstancesOfCourse(courseName: string): Observable<ICourseInstanceDTO[]> {
    const encodedName = encodeURIComponent(courseName);

    return this.http.get<ICourseInstanceDTO[]>(`${SERVER_API_URL}api/course-instance/instances/of/${encodedName}`);
  }

  /**
   * Returns the page data for the instance overview.
   *
   * @param courseName the course name
   * @param page the paging information
   */
  public getOverviewInstances(courseName: string, page: Pagination): Observable<HttpResponse<IDisplayableCourseInstanceDTO[]>> {
    const encodedName = encodeURIComponent(courseName);
    const options = createRequestOption(page);

    return this.http.get<IDisplayableCourseInstanceDTO[]>(`${SERVER_API_URL}api/course-instance/overview-instances/of/${encodedName}`, {
      params: options,
      observe: 'response',
    });
  }

  /**
   * Returns the assigned students of a course instance.
   *
   * @param courseInstanceId the internal course instance id
   */
  public getAssignedStudentsOfCourseInstance(courseInstanceId: string): Observable<IStudentInfoDTO[]> {
    const uuid = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);
    return this.http.get<IStudentInfoDTO[]>(`${SERVER_API_URL}api/course-instance/students/of/${uuid}`);
  }

  /**
   * Sets the assigned students for a course instance.
   *
   * @param courseInstanceId the internal course instance id
   * @param matriculationNumbers the list of matriculation numbers
   */
  public setAssignedStudents(courseInstanceId: string, matriculationNumbers: string[]): Observable<any> {
    return this.http.put(`${SERVER_API_URL}api/course-instance/students`, {
      courseInstanceId,
      matriculationNumbers,
    });
  }
}
