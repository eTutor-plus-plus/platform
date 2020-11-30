import { Component, OnInit, ViewChild } from '@angular/core';
import { LearningGoalsService } from './learning-goals.service';
import { LearningGoalTreeviewItem } from './learning-goal-treeview-item.model';
import { TreeviewComponent, TreeviewConfig, TreeviewI18n } from 'ngx-treeview';
import { ContextMenuComponent } from 'ngx-contextmenu';
import { LearningGoalCreationComponent } from './learning-goal-creation/learning-goal-creation.component';
import { AccountService } from '../../core/auth/account.service';
import { DefaultTreeviewI18n } from '../../shared/util/default-treeview-i18n';

/**
 * Component which is used for visualising the learning goals management.
 */
@Component({
  selector: 'jhi-learning-goals',
  templateUrl: './learning-goals.component.html',
  styleUrls: ['./learning-goals.component.scss'],
  providers: [{ provide: TreeviewI18n, useClass: DefaultTreeviewI18n }],
})
export class LearningGoalsComponent implements OnInit {
  @ViewChild(TreeviewComponent, { static: false })
  public treeviewComponent?: TreeviewComponent;
  @ViewChild('learningGoalCtxMenu')
  public learningGoalCtxMenu?: ContextMenuComponent;
  public learningGoals: LearningGoalTreeviewItem[] = [];
  public selectedLearningGoal?: LearningGoalTreeviewItem;
  public config = TreeviewConfig.create({
    hasAllCheckBox: false,
    hasFilter: true,
    hasCollapseExpand: true,
  });

  @ViewChild(LearningGoalCreationComponent)
  public learningGoalCreationComponent!: LearningGoalCreationComponent;
  public showCreateLearningGoalComponent = false;
  public username!: string;

  /**
   * Constructor
   *
   * @param learningGoalsService the injected learning goals service
   * @param accountService the injected account service
   */
  constructor(private learningGoalsService: LearningGoalsService, private accountService: AccountService) {}

  /**
   * Implements the on init method. See {@link OnInit}
   */
  public ngOnInit(): void {
    this.username = this.accountService.getLoginName() ?? '';

    this.loadLearningGoalsAsync();
  }

  /**
   * Loads all visible learning goals as tree view items asynchronously.
   */
  public async loadLearningGoalsAsync(): Promise<void> {
    this.learningGoals.length = 0;
    const list = await this.learningGoalsService.getAllVisibleLearningGoalsAsTreeViewItems(this.username).toPromise();
    list.forEach(x => this.learningGoals.push(x));
  }

  /**
   * Event handler which handles selected learning goal items.
   *
   * @param item the selected learning goal
   */
  public onSelect(item: LearningGoalTreeviewItem): void {
    this.selectedLearningGoal = item;
    this.showCreateLearningGoalComponent = false;
  }

  /**
   * Event handler which handles the create new sub goal event.
   *
   * @param parent the parent treeview item of the sub goal which should be created
   */
  public onCreateSubGoal(parent: LearningGoalTreeviewItem): void {
    this.learningGoalCreationComponent.subGoalCreationRequest(parent.text);
    this.showCreateLearningGoalComponent = true;
  }

  /**
   * Event handler which handles the creation request for a new goal.
   */
  public onCreateGoalRequested(): void {
    this.learningGoalCreationComponent.learningGoal = undefined;
    this.showCreateLearningGoalComponent = true;
  }

  /**
   * Event handler which handles the editing request for a goal.
   *
   * @param goalItem the goal item which should be edited
   */
  public onEditGoalRequested(goalItem: LearningGoalTreeviewItem): void {
    const goalModel = goalItem.toILearningGoalModel();

    this.learningGoalCreationComponent.learningGoal = goalModel;
    this.showCreateLearningGoalComponent = true;
  }

  /**
   * Checks whether the current user is allowed to edit the given goal or not.
   *
   * @param item the tree view item to check
   * @returns {@code true} if the current user is allowed to edit
   * the given goal, otherwise {@code false}
   */
  public isCurrentUserAllowedToEdit(item: LearningGoalTreeviewItem): boolean {
    return item.isUserAllowedToModify();
  }
}
