import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from '../../../app.constants';
import {
  ICourseInstanceInformationDTO,
  ICourseInstanceProgressOverviewDTO,
  IStudentFullNameInfoDTO,
  IStudentInfoDTO,
} from './students.model';
import { map } from 'rxjs/operators';

/**
 * Service for managing students.
 */
@Injectable({
  providedIn: 'root',
})
export class StudentService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Returns all available students.
   */
  public getAvailableStudents(): Observable<IStudentFullNameInfoDTO[]> {
    return this.http.get<IStudentInfoDTO[]>(`${SERVER_API_URL}api/student`).pipe(
      map(students =>
        students.map(x => {
          return {
            matriculationNumber: x.matriculationNumber,
            firstName: x.firstName,
            lastName: x.lastName,
            fullName: x.firstName + ' ' + x.lastName,
          };
        })
      )
    );
  }

  /**
   * Retrieves the courses of the currently logged-in student.
   */
  public getCourseInstancesOfLoggedInStudent(): Observable<ICourseInstanceInformationDTO[]> {
    return this.http.get<ICourseInstanceInformationDTO[]>(`${SERVER_API_URL}api/student/courses`);
  }

  /**
   * Returns the progress on course assignments from a given course of the currently logged-in
   * student.
   *
   * @param courseInstanceId the course instance URI
   */
  public getStudentCourseInstanceProgressOverview(courseInstanceId: string): Observable<ICourseInstanceProgressOverviewDTO[]> {
    const uuid = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);
    return this.http.get<ICourseInstanceProgressOverviewDTO[]>(`${SERVER_API_URL}api/student/courses/${uuid}/progress`);
  }
}
