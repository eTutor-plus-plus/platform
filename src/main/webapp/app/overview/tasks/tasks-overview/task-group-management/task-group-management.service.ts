import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  INewTaskGroupDTO,
  ITaskGroupDisplayDTO,
  ITaskGroupDTO,
} from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.model';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/core/request/request-util';
import { Pagination } from 'app/core/request/request.model';

/**
 * Service for managing task group operations.
 */
@Injectable({
  providedIn: 'root',
})
export class TaskGroupManagementService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Creates a new task group.
   *
   * @param newTaskGroup the task group to create
   */
  public createNewTaskGroup(newTaskGroup: INewTaskGroupDTO): Observable<ITaskGroupDTO> {
    return this.http.post<ITaskGroupDTO>(`${SERVER_API_URL}api/task-group`, newTaskGroup);
  }

  /**
   * Deletes a task group.
   *
   * @param name the task group's name
   */
  public deleteTaskGroup(name: string): Observable<any> {
    const encodedName = encodeURIComponent(name);

    return this.http.delete(`${SERVER_API_URL}api/task-group/${encodedName}`);
  }

  /**
   * Modifies a task group.
   *
   * @param taskGroup the task group to modify
   */
  public modifyTaskGroup(taskGroup: ITaskGroupDTO): Observable<ITaskGroupDTO> {
    return this.http.put<ITaskGroupDTO>(`${SERVER_API_URL}api/task-group`, taskGroup);
  }

  /**
   * Gets paged task groups.
   *
   * @param page the pagination info
   * @param query the optional fulltext filter
   */
  public getPagedTaskGroups(page: Pagination, query?: string): Observable<HttpResponse<ITaskGroupDisplayDTO>> {
    const options = createRequestOption(page);
    let url = `${SERVER_API_URL}api/task-group/displayable/list`;

    if (query && query.trim().length > 0) {
      url += `?filter=${query.trim()}`;
    }

    return this.http.get<ITaskGroupDisplayDTO>(url, { params: options, observe: 'response' });
  }
}
