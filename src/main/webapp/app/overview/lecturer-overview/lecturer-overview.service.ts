import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pagination } from '../../core/request/request.model';
import { createRequestOption } from '../../core/request/request-util';
import { SERVER_API_URL } from '../../app.constants';
import { IDisplayableCourseInstanceDTO } from '../course-management/course-mangement.model';
import { IStatisticsOverviewModelDTO } from './lecturer-overview.model';

/**
 * Service which is used for lecturer overview related functions.
 */
@Injectable({
  providedIn: 'root',
})
export class LecturerOverviewService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Returns the paged courses of the currently-logged in instructor.
   *
   * @param page the pagination information
   */
  public getPagedCoursesOfUser(page: Pagination): Observable<HttpResponse<IDisplayableCourseInstanceDTO[]>> {
    const options = createRequestOption(page);

    return this.http.get<IDisplayableCourseInstanceDTO[]>(`${SERVER_API_URL}api/lecturer-overview/courses`, {
      params: options,
      observe: 'response',
    });
  }

  /**
   * Returns the statistical overview from the given course instance.
   *
   * @param courseInstanceUUID the course instance's UUID
   */
  public getStatisticalOverviewOfCourseInstance(courseInstanceUUID: string): Observable<IStatisticsOverviewModelDTO> {
    return this.http.get<IStatisticsOverviewModelDTO>(`${SERVER_API_URL}api/lecturer-overview/statistics/${courseInstanceUUID}`);
  }
}
