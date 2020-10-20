import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { LearningGoalTreeviewItem } from "./learning-goal-treeview-item.model";
import { convertLearningGoal, ILearningGoalModel, INewLearningGoalModel } from "./learning-goal-model";
import { SERVER_API_URL } from "../../app.constants";
import { map } from "rxjs/operators";

/**
 * Service which retrieves the learning goals from the api
 * and is currently mocked. TODO: Add REST-API
 */
@Injectable({
  providedIn: 'root'
})
export class LearningGoalsService {

  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {
  }

  /**
   * Returns all visible learning goal tree view items.
   *
   * @returns an observable which contains the list of {@link LearningGoalTreeviewItem}.
   */
  public getAllVisibleLearningGoalsAsTreeViewItems(): Observable<LearningGoalTreeviewItem[]> {

    return this.http.get<ILearningGoalModel[]>(SERVER_API_URL + 'api/learninggoals').pipe(map(list => {
      const retList: LearningGoalTreeviewItem[] = [];

      for (const item of list) {
        retList.push(new LearningGoalTreeviewItem(convertLearningGoal(item)))
      }

      return retList;
    }));
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
}
