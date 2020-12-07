import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CourseModel, ILearningGoalUpdateAssignment } from './course-mangement.model';
import { SERVER_API_URL } from '../../app.constants';
import { LearningGoalTreeviewItem } from '../learning-goals/learning-goal-treeview-item.model';
import { convertLearningGoal, ILearningGoalModel } from '../learning-goals/learning-goal-model';
import { map } from 'rxjs/operators';

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
    return this.http.get<ILearningGoalModel[]>(SERVER_API_URL + `api/course/${course.name}/goals`).pipe(
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
}
