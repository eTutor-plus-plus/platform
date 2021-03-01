import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from '../../../app.constants';
import { CourseInstanceInformationDTO, IStudentFullNameInfoDTO, IStudentInfoDTO } from './students.model';
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
  public getCourseInstancesOfLoggedInStudent(): Observable<CourseInstanceInformationDTO[]> {
    return this.http.get<CourseInstanceInformationDTO[]>(`${SERVER_API_URL}api/student/courses`);
  }
}
