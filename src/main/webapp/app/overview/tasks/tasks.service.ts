import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { ITaskDisplayModel } from './tasks-overview/task.model';
import { Observable } from 'rxjs';
import { createRequestOption } from '../../shared/util/request-util';

type TaskDisplayResponseType = HttpResponse<ITaskDisplayModel>;
type TaskDisplayArrayResponseType = HttpResponse<ITaskDisplayModel[]>;

/**
 * Service which manages the tasks
 */
@Injectable({
  providedIn: 'root',
})
export class TasksService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Performs the REST endpoint call for retrieving the paged task display list.
   *
   * @param req the optional request options
   * @param headerFilter the optional header filter string
   */
  public queryTaskDisplayList(req?: any, headerFilter?: string): Observable<TaskDisplayArrayResponseType> {
    const options = createRequestOption(req);
    let url = 'api/tasks/display';

    if (headerFilter) {
      url += `?taskHeader=${headerFilter}`;
    }

    return this.http.get<ITaskDisplayModel[]>(url, { params: options, observe: 'response' });
  }
}
