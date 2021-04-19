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
import { createRequestOption, Pagination } from '../../../../../../shared/util/request-util';
import { SERVER_API_URL } from '../../../../../../app.constants';

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
}
