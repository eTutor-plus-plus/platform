import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable, of } from "rxjs";
import { LearningGoalTreeviewItem } from "./learning-goal-treeview-item.model";
import {ILearningGoalModel, INewLearningGoalModel} from "./learning-goal-model";
import {SERVER_API_URL} from "../../app.constants";

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
    const firstGoal = new LearningGoalTreeviewItem({
      text: 'First goal', value: 1, description: '', owner: 'admin', changeDate: new Date(), children: [{
        text: 'Sub goal 1', value: 2, description: '', owner: 'admin', changeDate: new Date()
      }, {
        text: 'Sub goal 2', value: 3, description: '', referencedFromCnt: 4, owner: 'admin', changeDate: new Date()
      }
      ]
    });

    const secondGoal = new LearningGoalTreeviewItem({
      text: 'Second goal', value: 4, description: '', markedAsPrivate: true,
      owner: 'admin', changeDate: new Date()
    });

    return of([firstGoal, secondGoal])
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
