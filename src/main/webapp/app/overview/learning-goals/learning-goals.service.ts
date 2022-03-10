import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LearningGoalTreeviewItem } from '../shared/learning-goal-treeview-item.model';
import { convertLearningGoal, ILearningGoalModel, INewLearningGoalModel } from '../shared/learning-goal-model';
import { SERVER_API_URL } from '../../app.constants';
import { map } from 'rxjs/operators';

/**
 * Service which manages the learning goals.
 */
@Injectable({
  providedIn: 'root',
})
export class LearningGoalsService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Returns all visible learning goal tree view items.
   *
   * @param userLogin the login of the current user
   * @param onlyShowOwnGoals indicated whether only the currently logged-in user's goals
   * should be displayed (default: false)
   * @returns an observable which contains the list of {@link LearningGoalTreeviewItem}.
   */
  public getAllVisibleLearningGoalsAsTreeViewItems(userLogin: string, onlyShowOwnGoals = false): Observable<LearningGoalTreeviewItem[]> {
    const url = `${SERVER_API_URL}api/learninggoals?showOnlyOwnGoals=${onlyShowOwnGoals ? 'true' : 'false'}`;

    return this.http.get<ILearningGoalModel[]>(url).pipe(
      map(list => {
        const retList: LearningGoalTreeviewItem[] = [];

        for (const item of list) {
          retList.push(new LearningGoalTreeviewItem(convertLearningGoal(item), userLogin));
        }

        return retList;
      })
    );
  }

  /**
   * Saves a new learning goal.
   *
   * @param newGoal the learning goal which should be saved
   * @returns an observable which contains the {@link ILearningGoalModel}
   */
  public postNewLearningGoal(newGoal: INewLearningGoalModel): Observable<ILearningGoalModel> {
    return this.http.post<ILearningGoalModel>(SERVER_API_URL + 'api/learninggoals', newGoal);
  }

  /**
   * Updates an existing learning goal.
   *
   * @param goal the goal which should be updated
   * @returns an observable which contains an empty response object
   */
  public updateLearningGoal(goal: ILearningGoalModel): Observable<any> {
    return this.http.put(SERVER_API_URL + 'api/learninggoals', goal);
  }

  /**
   * Creates a new sub goal.
   *
   * @param newGoal the sub goal which should be saved
   * @param parentGoalName the name of the parent goal
   * @param currentUser the current logged in user
   * @returns an observable which contains the {@link ILearningGoalModel}
   */
  public createSubGoal(newGoal: INewLearningGoalModel, parentGoalName: string, currentUser: string): Observable<ILearningGoalModel> {
    const encodedParentGoalName = encodeURIComponent(parentGoalName);

    return this.http.post<ILearningGoalModel>(
      SERVER_API_URL + `api/learninggoals/${currentUser}/${encodedParentGoalName}/subGoal`,
      newGoal
    );
  }

  /**
   * Sets the dependencies of a given goal.
   *
   * @param owner the goal's owner
   * @param goalName the goal's name
   * @param dependencies the dependency ids
   */
  public setDependencies(owner: string, goalName: string, dependencies: string[]): Observable<HttpResponse<any>> {
    const encodedName = encodeURIComponent(goalName);
    return this.http.put(SERVER_API_URL + `api/learninggoals/${owner}/${encodedName}/dependencies`, dependencies, { observe: 'response' });
  }

  /**
   * Returns the dependencies of a given goal.
   *
   * @param owner the goal's owner
   * @param goalName the goal's name
   */
  public getDependencies(owner: string, goalName: string): Observable<HttpResponse<string[]>> {
    const encodedName = encodeURIComponent(goalName);
    return this.http.get<string[]>(SERVER_API_URL + `api/learninggoals/${owner}/${encodedName}/dependencies`, { observe: 'response' });
  }

  /**
   * Returns the displayable dependencies of a given goal.
   *
   * @param owner the goal's owner
   * @param goalName the goal's na,e
   */
  public getDisplayableDependencies(owner: string, goalName: string): Observable<HttpResponse<string[]>> {
    const encodedName = encodeURIComponent(goalName);
    return this.http.get<string[]>(SERVER_API_URL + `api/learninggoals/${owner}/${encodedName}/dependencies/displayable`, {
      observe: 'response',
    });
  }

  /**
   * Deletes the given goal and its sub goals.
   *
   * @param goalName the goal's name
   */
  public deleteGoalWithSubGoals(goalName: string): Observable<HttpResponse<any>> {
    const encodedName = encodeURIComponent(goalName);
    return this.http.delete(`${SERVER_API_URL}api/learninggoals/${encodedName}`, { observe: 'response' });
  }

  public addGoalAsSubGoal(
    goalOwner: string,
    goalName: string,
    parentGoalOwner: string,
    parentGoalName: string
  ): Observable<HttpResponse<any>> {
    const encodedGoalName = encodeURIComponent(goalName);
    const encodedParentGoalName = encodeURIComponent(parentGoalName);
    return this.http.post(
      SERVER_API_URL + `api/learninggoals/${parentGoalOwner}/parentGoal/${encodedParentGoalName}/${goalOwner}/subGoal/${encodedGoalName}`,
      null,
      { observe: 'response' }
    );
  }
}
