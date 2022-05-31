import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { INewTaskModel, ITaskAssignmentDisplay, ITaskDisplayModel, ITaskModel } from './task.model';
import { Observable } from 'rxjs';
import { createRequestOption } from 'app/core/request/request-util';

type TaskDisplayArrayResponseType = HttpResponse<ITaskDisplayModel[]>;
type TaskResponseType = HttpResponse<ITaskModel>;
type StringArrayResponseType = HttpResponse<string[]>;
type TaskAssignmentDisplayResponseType = HttpResponse<ITaskAssignmentDisplay[]>;

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
   * @param taskGroupHeaderFilter the optional task group header filter
   */
  public queryTaskDisplayList(req?: any, headerFilter?: string, taskGroupHeaderFilter?: string): Observable<TaskDisplayArrayResponseType> {
    const options = createRequestOption(req);
    let url = 'api/tasks/display';

    if (headerFilter && headerFilter.trim().length > 0) {
      url += `?taskHeader=${headerFilter.trim()}`;
    }

    if (taskGroupHeaderFilter && taskGroupHeaderFilter.trim().length > 0) {
      if (headerFilter && headerFilter.trim().length > 0) {
        url += '&';
      } else {
        url += '?';
      }

      url += `taskGroupHeader=${taskGroupHeaderFilter.trim()}`;
    }

    return this.http.get<ITaskDisplayModel[]>(url, { params: options, observe: 'response' });
  }

  /**
   * Performs the REST endpoint call for retrieving a task assingment
   * object by its id.
   *
   * @param internalId the internal task's id
   * @param alreadyParsed indicates whether the given internalId is already parsed or not (default = false)
   */
  public getTaskAssignmentById(internalId: string, alreadyParsed = false): Observable<TaskResponseType> {
    let id;

    if (alreadyParsed) {
      id = internalId;
    } else {
      id = internalId.substr(internalId.lastIndexOf('#') + 1);
    }

    return this.http.get<ITaskModel>(`api/tasks/assignments/${id}`, { observe: 'response' });
  }

  /**
   * returns the calc solution file id
   *
   * @param internalId the internal task's id
   * @param alreadyParsed indicates whether the given internalId is already parsed or not (default = false)
   */
  public getFileIdOfCalcSolution(internalId: string, alreadyParsed = false): Observable<number> {
    let id;

    if (alreadyParsed) {
      id = internalId;
    } else {
      id = internalId.substr(internalId.lastIndexOf('#') + 1);
    }

    return this.http.get<number>(`api/tasks/assignments/calc_solution/${id}`);
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

  /**
   * Returns the ids of the assigned learning goals.
   *
   * @param internalId the internal task's id
   */
  public getAssignedLearningGoalsOfAssignment(internalId: string): Observable<StringArrayResponseType> {
    const id = internalId.substr(internalId.lastIndexOf('#') + 1);

    return this.http.get<string[]>(`api/tasks/assignments/${id}/learninggoals`, { observe: 'response' });
  }

  /**
   * Saves assigned learning goal ids for a specific task assignment.
   *
   * @param internalId the task's internal id
   * @param learningGoalIds the learning goal ids
   */
  public saveAssignedLearningGoalIdsForTask(internalId: string, learningGoalIds: string[]): Observable<HttpResponse<any>> {
    const id = internalId.substr(internalId.lastIndexOf('#') + 1);

    return this.http.put(`api/tasks/assignments/${id}`, learningGoalIds, { observe: 'response' });
  }

  /**
   * Deletes the given assignment by its id.
   *
   * @param internalId the internal assignment's id
   */
  public deleteAssignment(internalId: string): Observable<HttpResponse<any>> {
    const id = internalId.substr(internalId.lastIndexOf('#') + 1);

    return this.http.delete(`api/tasks/assignments/${id}`, { observe: 'response' });
  }

  /**
   * Returns the associated tasks of a given learning goal.
   *
   * @param goalName the learning goal's name
   * @param goalOwner the learning goal's owner
   */
  public getTasksOfLearningGoal(goalName: string, goalOwner: string): Observable<TaskAssignmentDisplayResponseType> {
    const encodedName = encodeURIComponent(goalName);

    return this.http.get<ITaskAssignmentDisplay[]>(`api/tasks/of/${goalOwner}/${encodedName}`, { observe: 'response' });
  }
}
