import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LearningGoalTreeviewItem } from './learning-goal-treeview-item.model';
import { convertLearningGoal, ILearningGoalModel, INewLearningGoalModel } from './learning-goal-model';
import { SERVER_API_URL } from '../../app.constants';
import { map } from 'rxjs/operators';

/**
 * Service which retrieves the learning goals from the api
 * and is currently mocked.
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
   * @returns an observable which contains the list of {@link LearningGoalTreeviewItem}.
   */
  public getAllVisibleLearningGoalsAsTreeViewItems(userLogin: string): Observable<LearningGoalTreeviewItem[]> {
    return this.http.get<ILearningGoalModel[]>(SERVER_API_URL + 'api/learninggoals').pipe(
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
  public updateLearningGoal(goal: ILearningGoalModel): Observable<Object> {
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
}
