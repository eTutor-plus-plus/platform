import { Component, OnInit } from '@angular/core';
import { TasksService } from '../../tasks.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { LearningGoalsService } from '../../../learning-goals/learning-goals.service';
import { AccountService } from '../../../../core/auth/account.service';
import { LearningGoalTreeviewItem } from '../../../shared/learning-goal-treeview-item.model';
import { ITaskDisplayModel } from '../../task.model';

/**
 * Component which represents a task assignment update modal window.
 */
@Component({
  selector: 'jhi-task-assignment-update',
  templateUrl: './task-assignment-update.component.html',
  styleUrls: ['./task-assignment-update.component.scss'],
})
export class TaskAssignmentUpdateComponent implements OnInit {
  public isSaving = false;

  public learningGoals: LearningGoalTreeviewItem[] = [];
  public selectedGoals: string[] = [];

  private loginName = '';
  private _taskDisplayModel?: ITaskDisplayModel;

  /**
   * Constructor.
   *
   * @param tasksService the injected tasks service
   * @param activeModal the injected active modal service
   * @param learningGoalsService the injected learning goals service
   * @param accountService the injected account service
   */
  constructor(
    private tasksService: TasksService,
    private activeModal: NgbActiveModal,
    private learningGoalsService: LearningGoalsService,
    private accountService: AccountService
  ) {
    this.loginName = this.accountService.getLoginName()!;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.learningGoalsService.getAllVisibleLearningGoalsAsTreeViewItems(this.loginName).subscribe(value => (this.learningGoals = value));
  }

  /**
   * Saves the current assignment.
   */
  public save(): void {
    this.isSaving = true;

    this.tasksService.saveAssignedLearningGoalIdsForTask(this.taskDisplayModel!.taskId, this.selectedGoals).subscribe(
      () => {
        this.isSaving = false;
        this.close();
      },
      () => {
        this.isSaving = false;
      }
    );
  }

  /**
   * Closes the dialog.
   */
  public close(): void {
    this.activeModal.close();
  }

  /**
   * Handles the click event and selects or deselects items.
   *
   * @param event the mouse event
   * @param item the item to select or deselect
   */
  public handleGoalClicked(event: MouseEvent, item: LearningGoalTreeviewItem): void {
    const idx = this.selectedGoals.indexOf(item.value);

    if (event.ctrlKey) {
      if (idx > -1) {
        this.selectedGoals.splice(idx, 1);
      }
    } else {
      if (idx === -1) {
        this.selectedGoals.push(item.value);
      } else {
        this.selectedGoals.splice(idx, 1);
      }
    }
  }

  /**
   * Sets the task display model.
   *
   * @param value the value to set
   */
  public set taskDisplayModel(value: ITaskDisplayModel | undefined) {
    this._taskDisplayModel = value;

    if (value) {
      this.tasksService.getAssignedLearningGoalsOfAssignment(value.taskId).subscribe(response => {
        if (response.body) {
          this.selectedGoals = response.body;
        }
      });
    }
  }

  /**
   * Returns the task display model.
   */
  public get taskDisplayModel(): ITaskDisplayModel | undefined {
    return this._taskDisplayModel;
  }
}
