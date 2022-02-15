import { Component, OnInit } from '@angular/core';
import { LearningGoalsService } from '../../../../learning-goals/learning-goals.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { LearningGoalTreeviewItem } from '../../../../shared/learning-goal-treeview-item.model';
import { AccountService } from '../../../../../core/auth/account.service';

/**
 * Component for displaying assigned goals
 */

@Component({
  selector: 'jhi-student-exercise-sheet-goals',
  templateUrl: './student-exercise-sheet-goals.component.html',
  styleUrls: ['./student-exercise-sheet-goals.component.scss'],
})
export class StudentExerciseSheetGoalsComponent implements OnInit {
  public allLearningGoals: LearningGoalTreeviewItem[] = [];
  public filteredGoals: LearningGoalTreeviewItem[] = [];

  private _assignedGoals: string[] = [];
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
      this.allLearningGoals = value;
      this.filteredGoals = this.allLearningGoals.filter(g => this.assignedGoals.includes(g.text));
    });
  }

  /**
   * Sets the assigned learning goals
   * @param value
   */
  public set assignedGoals(value: string[]) {
    this._assignedGoals = value;
  }

  /**
   * Returns the assigned learning goals
   */
  public get assignedGoals(): string[] {
    return this._assignedGoals;
  }

  /**
   * Closes the modal dialog.
   */
  public cancel(): void {
    this.activeModal.dismiss();
  }
}
