import { Component, OnInit } from '@angular/core';
import { LearningGoalsService } from '../learning-goals.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { LearningGoalTreeviewItem } from '../../shared/learning-goal-treeview-item.model';
import { AccountService } from '../../../core/auth/account.service';

/**
 * Component for managing learning goal dependencies.
 */
@Component({
  selector: 'jhi-dependency-manager-window',
  templateUrl: './dependency-manager-window.component.html',
  styleUrls: ['./dependency-manager-window.component.scss'],
})
export class DependencyManagerWindowComponent implements OnInit {
  public selectedGoals: string[] = [];
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
    this.learningGoalsService.getAllVisibleLearningGoalsAsTreeViewItems(this._loginName).subscribe(value => (this.learningGoals = value));
  }

  /**
   * Saves the dependencies.
   */
  public save(): void {
    this.isSaving = true;

    this.learningGoalsService.setDependencies(this.currentGoal.owner, this.currentGoal.text, this.selectedGoals).subscribe(
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
   * Sets the current selected learning goal.
   *
   * @param value the goal from the treeview
   */
  public set currentGoal(value: LearningGoalTreeviewItem) {
    this._goalItem = value;

    this.learningGoalsService.getDependencies(value.owner, value.text).subscribe(response => (this.selectedGoals = response.body!));
  }

  /**
   * Returns the current selected goal.
   */
  public get currentGoal(): LearningGoalTreeviewItem {
    return this._goalItem!;
  }

  /**
   * Handles the goal click event.
   *
   * @param event the mouse event
   * @param item the selected goal item
   */
  public handleGoalClicked(event: MouseEvent, item: LearningGoalTreeviewItem): void {
    if (item.value !== this.currentGoal.value) {
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
  }
}
