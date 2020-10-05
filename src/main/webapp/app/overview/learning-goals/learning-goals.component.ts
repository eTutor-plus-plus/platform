import { Component, OnInit, ViewChild } from '@angular/core';
import { LearningGoalsService } from "./learning-goals.service";
import { LearningGoalTreeviewItem } from "./learning-goal-treeview-item.model";
import { TreeviewComponent, TreeviewConfig } from "ngx-treeview";
import {ContextMenuComponent} from "ngx-contextmenu";

/**
 * Component which is used for visualising the learning goals management.
 */
@Component({
  selector: 'jhi-learning-goals',
  templateUrl: './learning-goals.component.html',
  styleUrls: ['./learning-goals.component.scss']
})
export class LearningGoalsComponent implements OnInit {

  @ViewChild(TreeviewComponent, {static: false})
  public treeviewComponent?: TreeviewComponent;
  @ViewChild('learningGoalCtxMenu')
  public learningGoalCtxMenu?: ContextMenuComponent;
  public learningGoals: LearningGoalTreeviewItem[] = [];
  public config = TreeviewConfig.create({
    hasAllCheckBox: false,
    hasFilter: true,
    hasCollapseExpand: true
  });

  /**
   * Constructor
   *
   * @param learningGoalsService the injected learning goals service
   */
  constructor(private learningGoalsService: LearningGoalsService) { }

  /**
   * Implements the on init method. See {@link OnInit}
   */
  public ngOnInit(): void {
    this.loadLearningGoalsAsync();
  }

  /**
   * Loads all visible learning goals as tree view items asynchronously.
   */
  private async loadLearningGoalsAsync(): Promise<void> {
    this.learningGoals.length = 0;
    const list = await this.learningGoalsService.getAllVisibleLearningGoalsAsTreeViewItems().toPromise();
    list.forEach(x => this.learningGoals.push(x));
  }

  /**
   * Event handler which handles selected learning goal items.
   *
   * @param item the selected learning goal
   */
  public onSelect(item: LearningGoalTreeviewItem): void {
    // eslint-disable-next-line no-console
    console.log('selection changed ' + JSON.stringify(item));
  }

  /**
   * Event handler which handles the create new sub goal event.
   *
   * @param parent the parent treeview item of the sub goal which should be created
   */
  public onCreateSubGoal(parent: LearningGoalTreeviewItem): void {
    // eslint-disable-next-line no-console
    console.log('onCreateSubGoal for ' + JSON.stringify(parent));
  }
}
