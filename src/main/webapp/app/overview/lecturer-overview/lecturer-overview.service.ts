import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ICourseOverviewModel } from './lecturer-overview.model';
import { Pagination } from '../../core/request/request.model';
import { createRequestOption } from '../../core/request/request-util';
import { SERVER_API_URL } from '../../app.constants';

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
  public getPagedCoursesOfUser(page: Pagination): Observable<HttpResponse<ICourseOverviewModel[]>> {
    const options = createRequestOption(page);

    return this.http.get<ICourseOverviewModel[]>(`${SERVER_API_URL}api/lecturer-overview/courses`, {
      params: options,
      observe: 'response',
    });
  }
}
