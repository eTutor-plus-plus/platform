import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  IGradingInfoVM,
  ILecturerGradingInfo,
  ILecturerStudentTaskAssignmentInfoModel,
  ILecturerTaskAssignmentInfoModel,
  IStudentAssignmentOverviewInfo,
} from './lecturer-task-assignment.model';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { Pagination } from 'app/core/request/request.model';
import { createRequestOption } from 'app/core/request/request-util';
import { TaskPointEntryModel } from '../task-point-entry.model';

/**
 * Service for lecturer task assignment related operations.
 */
@Injectable({
  providedIn: 'root',
})
export class LecturerTaskAssignmentService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Returns the paged student assignment overview info.
   *
   * @param lecturerAssignmentInfoModel the lecturer assignment info model
   * @param page the paging info
   */
  public getStudentAssignmentInfoPage(
    lecturerAssignmentInfoModel: ILecturerTaskAssignmentInfoModel,
    page: Pagination
  ): Observable<HttpResponse<IStudentAssignmentOverviewInfo[]>> {
    const courseInstanceUUID = lecturerAssignmentInfoModel.courseInstanceId.substr(
      lecturerAssignmentInfoModel.courseInstanceId.lastIndexOf('#') + 1
    );
    const exerciseSheetUUID = lecturerAssignmentInfoModel.exerciseSheetId.substr(
      lecturerAssignmentInfoModel.exerciseSheetId.lastIndexOf('#') + 1
    );

    const options = createRequestOption(page);
    return this.http.get<IStudentAssignmentOverviewInfo[]>(
      `${SERVER_API_URL}api/lecturer/overview/${courseInstanceUUID}/${exerciseSheetUUID}`,
      {
        params: options,
        observe: 'response',
      }
    );
  }

  /**
   * Returns the grading info for a lecturer.
   *
   * @param lecturerStudentTaskInfoModel the lecturer student task info model
   */
  public getGradingInfo(
    lecturerStudentTaskInfoModel: ILecturerStudentTaskAssignmentInfoModel
  ): Observable<HttpResponse<ILecturerGradingInfo[]>> {
    const courseInstanceUUID = lecturerStudentTaskInfoModel.courseInstanceId.substr(
      lecturerStudentTaskInfoModel.courseInstanceId.lastIndexOf('#') + 1
    );
    const exerciseSheetUUID = lecturerStudentTaskInfoModel.exerciseSheetId.substr(
      lecturerStudentTaskInfoModel.exerciseSheetId.lastIndexOf('#') + 1
    );

    return this.http.get<ILecturerGradingInfo[]>(
      `${SERVER_API_URL}api/lecturer/grading/${courseInstanceUUID}/${exerciseSheetUUID}/${lecturerStudentTaskInfoModel.matriculationNo}`,
      { observe: 'response' }
    );
  }

  /**
   * Sets the grade for an assignment task.
   *
   * @param gradingInfoVM vm containing the grading info
   */
  public setGradeForAssignment(gradingInfoVM: IGradingInfoVM): Observable<any> {
    return this.http.put(`${SERVER_API_URL}api/lecturer/grading`, gradingInfoVM);
  }

  /**
   * Returns a student's assingment files's id.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet UUID
   * @param taskNo the task no
   * @param matriculationNo the student's matriculation number
   */
  public getFileIdOfStudentsAssignment(
    courseInstanceId: string,
    exerciseSheetUUID: string,
    taskNo: number,
    matriculationNo: string
  ): Observable<number> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.get<number>(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/uploadTask/${taskNo}/file-attachment/of-student/${matriculationNo}`
    );
  }

  /**
   * Returns an overview about all the assigned tasks and the achieved points/ max points for an exercise sheet and course instance
   * @param courseInstanceId the course instance
   * @param exerciseSheetUUID the exercise sheet
   */
  public getExerciseSheetPointOverview(courseInstanceId: string, exerciseSheetUUID: string): Observable<TaskPointEntryModel[]> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);
    return this.http.get<TaskPointEntryModel[]>(
      `${SERVER_API_URL}api/lecturer/course-instance/${instanceUUID}/exercise-sheet/${exerciseSheetUUID}/points-overview`,
      { responseType: 'text' as 'json' }
    );
  }
}
