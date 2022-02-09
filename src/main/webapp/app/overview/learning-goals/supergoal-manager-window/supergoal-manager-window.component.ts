import { Component, OnInit } from '@angular/core';
import { LearningGoalsService } from '../learning-goals.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { LearningGoalTreeviewItem } from '../../shared/learning-goal-treeview-item.model';
import { AccountService } from '../../../core/auth/account.service';

/**
 * Component for managing super-goal-assignments
 */

@Component({
  selector: 'jhi-dependency-manager-window',
  templateUrl: './supergoal-manager-window.component.html',
  styleUrls: ['./supergoal-manager-window.component.scss'],
})
export class SupergoalManagerWindowComponent implements OnInit {
  public selectedGoal = '';
  public selectedGoalOwner = '';
  public learningGoals: LearningGoalTreeviewItem[] = [];
  public isSaving = false;

  private _goalItem?: LearningGoalTreeviewItem;
  private _loginName = '';

  /**
   * Constructor.
   *
   * @param learningGoalsService the injected learning goal service
   * @param activeModal the injected active modal service
   * @param accountService the injected account service
   */
  constructor(
    private learningGoalsService: LearningGoalsService,
    private activeModal: NgbActiveModal,
    private accountService: AccountService
  ) {
    this._loginName = this.accountService.getLoginName()!;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.learningGoalsService.getAllVisibleLearningGoalsAsTreeViewItems(this._loginName).subscribe(value => {
      this.learningGoals = value;
    });
  }

  /**
   * Sets the current selected learning goal.
   *
   * @param value the goal from the treeview
   */
  public set currentGoal(value: LearningGoalTreeviewItem) {
    this._goalItem = value;
  }

  /**
   * Returns the current selected goal.
   */
  public get currentGoal(): LearningGoalTreeviewItem {
    return this._goalItem!;
  }

  /**
   * Saves the new relation.
   */
  public save(): void {
    this.isSaving = true;
    this.learningGoalsService
      .addGoalAsSubGoal(this.currentGoal.owner, this.currentGoal.text, this.selectedGoalOwner, this.selectedGoal)
      .subscribe(
        () => {
          this.isSaving = false;
          this.activeModal.close();
        },
        () => (this.isSaving = false)
      );
  }

  /**
   * Closes the modal dialog.
   */
  public cancel(): void {
    this.activeModal.dismiss();
  }

  /**
   * Handles the goal click event.
   *
   * @param event the mouse event
   * @param item the selected goal item
   */
  public handleGoalClicked(event: MouseEvent, item: any): void {
    if (item.value !== this.currentGoal.value) {
      if (item.value === this.selectedGoal) {
        this.selectedGoal = '';
        this.selectedGoalOwner = '';
      } else {
        this.selectedGoal = item.text;
        this.selectedGoalOwner = item.owner;
      }
    }
  }
}
