import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { INewTaskModel, ITaskDisplayModel, ITaskModel } from './task.model';
import { Observable } from 'rxjs';
import { createRequestOption } from '../../shared/util/request-util';

type TaskDisplayResponseType = HttpResponse<ITaskDisplayModel>;
type TaskDisplayArrayResponseType = HttpResponse<ITaskDisplayModel[]>;
type TaskResponseType = HttpResponse<ITaskModel>;

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

  /**
   * Performs the REST endpoint call for retrieving a task assingment
   * object by its id.
   *
   * @param internalId the internal task assingment object
   */
  public getTaskAssignmentById(internalId: string): Observable<TaskResponseType> {
    const id = internalId.substr(internalId.lastIndexOf('#') + 1);

    return this.http.get<ITaskModel>(`api/tasks/assignments/${id}`, { observe: 'response' });
  }

  /**
   * Saves a new task.
   *
   * @param task the task to save
   */
  public saveNewTask(task: INewTaskModel): Observable<HttpResponse<any>> {
    return this.http.post('api/tasks/assignments', task, { observe: 'response' });
  }

  /**
   * Saves an edited tasks.
   *
   * @param task the task to save
   */
  public saveEditedTask(task: ITaskModel): Observable<HttpResponse<any>> {
    return this.http.put('api/tasks/assignments', task, { observe: 'response' });
  }
}
