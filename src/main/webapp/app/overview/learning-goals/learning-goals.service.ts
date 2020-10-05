import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable, of } from "rxjs";
import { LearningGoalTreeviewItem } from "./learning-goal-treeview-item.model";

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
      text: 'First goal', value: 1, description: '', children: [{
        text: 'Sub goal 1', value: 2, description: ''
      }, {
        text: 'Sub goal 2', value: 3, description: ''
      }
      ]
    });

    const secondGoal = new LearningGoalTreeviewItem({
      text: 'Second goal', value: 4, description: '', markedAsPrivate: true
    });

    return of([firstGoal, secondGoal])
  }
}
