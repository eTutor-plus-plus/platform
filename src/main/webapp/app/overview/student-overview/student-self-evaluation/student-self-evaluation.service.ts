import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from 'app/app.constants';
import { IStudentSelfEvaluationLearningGoal } from './student-self-evaluation.model';

/**
 * Service for the student self evaluation.
 */
@Injectable({
  providedIn: 'root',
})
export class StudentSelfEvaluationService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Saves a student's self evaluation.
   *
   * @param courseInstanceId the corresponding course instance id
   * @param goals the corresponding goal entries
   */
  public saveEvaluation(courseInstanceId: string, goals: IStudentSelfEvaluationLearningGoal[]): Observable<any> {
    const uuid = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.post(`${SERVER_API_URL}api/student/courses/${uuid}/self-evaluation`, goals);
  }
}
