import { Component, OnInit, ViewChild } from '@angular/core';
import { LearningGoalsService } from './learning-goals.service';
import { LearningGoalTreeviewItem } from '../shared/learning-goal-treeview-item.model';
import { ContextMenuComponent } from 'ngx-contextmenu';
import { LearningGoalCreationComponent } from './learning-goal-creation/learning-goal-creation.component';
import { AccountService } from 'app/core/auth/account.service';
import { TasksService } from '../tasks/tasks.service';
import { ITaskAssignmentDisplay } from '../tasks/task.model';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DependencyManagerWindowComponent } from './dependency-manager-window/dependency-manager-window.component';
import { combineLatest } from 'rxjs';
import { AlertService } from 'app/core/util/alert.service';
import { TreeviewComponent } from '../../shared/ngx-treeview/components/treeview/treeview.component';
import { SupergoalManagerWindowComponent } from './supergoal-manager-window/supergoal-manager-window.component';

/**
 * Component which is used for visualising the learning goals management.
 */
@Component({
  selector: 'jhi-learning-goals',
  templateUrl: './learning-goals.component.html',
  styleUrls: ['./learning-goals.component.scss'],
})
export class LearningGoalsComponent implements OnInit {
  @ViewChild(TreeviewComponent, { static: false })
  public treeviewComponent?: TreeviewComponent;
  @ViewChild('learningGoalCtxMenu')
  public learningGoalCtxMenu?: ContextMenuComponent;
  public learningGoals: LearningGoalTreeviewItem[] = [];
  public selectedLearningGoal?: LearningGoalTreeviewItem;
  public learningGoalTasks: ITaskAssignmentDisplay[] = [];
  public learningGoalDependencies: string[] = [];

  @ViewChild(LearningGoalCreationComponent)
  public learningGoalCreationComponent!: LearningGoalCreationComponent;
  public showCreateLearningGoalComponent = false;
  public username!: string;

  public showOnlyUserGoals = false;

  /**
   * Constructor
   *
   * @param learningGoalsService the injected learning goals service
   * @param accountService the injected account service
   * @param tasksService the injected task service
   * @param modalService the injected modal service
   * @param alertService the injected alert service
   */
  constructor(
    private learningGoalsService: LearningGoalsService,
    private accountService: AccountService,
    private tasksService: TasksService,
    private modalService: NgbModal,
    private alertService: AlertService
  ) {}

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
    const list = await this.learningGoalsService
      .getAllVisibleLearningGoalsAsTreeViewItems(this.username, this.showOnlyUserGoals)
      .toPromise();
    list.forEach(x => this.learningGoals.push(x));
  }

  /**
   * Event handler which handles selected learning goal items.
   *
   * @param item the selected learning goal
   */
  public onSelect(item: LearningGoalTreeviewItem): void {
    this.learningGoalTasks = [];
    const obs1 = this.tasksService.getTasksOfLearningGoal(item.text, item.owner);
    const obs2 = this.learningGoalsService.getDisplayableDependencies(item.owner, item.text);

    combineLatest([obs1, obs2]).subscribe(([ret1, ret2]) => {
      if (ret1.body && ret2.body) {
        this.learningGoalTasks = ret1.body;
        this.learningGoalDependencies = ret2.body;

        this.selectedLearningGoal = item;
        this.showCreateLearningGoalComponent = false;
      }
    });
  }

  /**
   * Event handler which handles the create new sub goal event.
   *
   * @param parent the parent treeview item of the sub goal which should be created
   */
  public onCreateSubGoal(parent: LearningGoalTreeviewItem): void {
    this.learningGoalCreationComponent.clear();
    this.learningGoalCreationComponent.subGoalCreationRequest(parent.text);
    this.showCreateLearningGoalComponent = true;
  }

  /**
   * Event handler which handles the creation request for a new goal.
   */
  public onCreateGoalRequested(): void {
    this.learningGoalCreationComponent.clear();
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
    this.learningGoalCreationComponent.clear();
    this.learningGoalCreationComponent.learningGoal = goalModel;
    this.showCreateLearningGoalComponent = true;
  }

  /**
   * Event handler which handles the delete learning goal event.
   *
   * @param goalItem the goal item which should be deleted
   */
  public onDelete(goalItem: LearningGoalTreeviewItem): void {
    (async () => {
      await this.learningGoalsService.deleteGoalWithSubGoals(goalItem.text).toPromise();
      await this.loadLearningGoalsAsync();
      this.alertService.addAlert({
        type: 'success',
        translationKey: 'learningGoalManagement.learningGoalRemovedMsg',
        translationParams: { name: goalItem.text },
        timeout: 5000,
      });
    })();
  }

  /**
   * Checks the whether given goal can be removed or not
   *
   * @param goalItem the goal item to check
   */
  public canGoalBeRemoved = (goalItem: LearningGoalTreeviewItem): boolean =>
    goalItem.isUserAllowedToModify() && this.getReferenceCntRecursive(goalItem) === 0;

  /**
   * Event handler which handles the dependency manager window request for a goal.
   *
   * @param goalItem the selected goal item
   */
  public onManageDependentGoalsRequested(goalItem: LearningGoalTreeviewItem): void {
    const modalRef = this.modalService.open(DependencyManagerWindowComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as DependencyManagerWindowComponent).currentGoal = goalItem;
    modalRef.closed.subscribe(() => {
      this.onSelect(goalItem);
    });
  }

  public onManageSuperGoalRequested(goalItem: LearningGoalTreeviewItem): void {
    const modalRef = this.modalService.open(SupergoalManagerWindowComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as SupergoalManagerWindowComponent).currentGoal = goalItem;
    modalRef.closed.subscribe(() => {
      this.onSelect(goalItem);
      this.loadLearningGoalsAsync();
    });
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

  /**
   * Event handler for filter changes
   */
  public onFilterChanged(): void {
    this.loadLearningGoalsAsync();
  }

  /**
   * Returns the recursive cumulated reference count.
   *
   * @param goalItem the goal item to check
   */
  private getReferenceCntRecursive(goalItem: LearningGoalTreeviewItem): number {
    let count = goalItem.referencedFromCnt;
    for (const child of goalItem.childItems) {
      count += this.getReferenceCntRecursive(child);
    }

    return count;
  }
}
