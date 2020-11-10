import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CourseModel } from './course-mangement.model';
import { SERVER_API_URL } from '../../app.constants';

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
}
