import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ILecturerTaskAssignmentInfoModel, IStudentAssignmentOverviewInfo } from './lecturer-task-assignment.model';
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

    // TODO: Include uuids in URL

    const options = createRequestOption(page);
    return this.http.get<IStudentAssignmentOverviewInfo[]>(`${SERVER_API_URL}api/`, {
      params: options,
      observe: 'response',
    });
  }
}
