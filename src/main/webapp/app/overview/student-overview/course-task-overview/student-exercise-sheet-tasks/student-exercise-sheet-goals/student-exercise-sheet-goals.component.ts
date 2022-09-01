import { Component, OnInit } from '@angular/core';
import { LearningGoalsService } from '../../../../learning-goals/learning-goals.service';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { LearningGoalTreeviewItem } from '../../../../shared/learning-goal-treeview-item.model';
import { AccountService } from '../../../../../core/auth/account.service';
import { CourseManagementService } from '../../../../course-management/course-management.service';
import { LearningGoalDescriptionModalComponent } from '../../../../learning-goals/learning-goal-description-modal/learning-goal-description-modal.component';

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
  public header = '';
  public courseName = '';
  public useOnlyCourseGoals = false;
  public filterGoalTrees = true;

  private _assignedGoals: string[] = [];
  private _loginName = '';

  /**
   * Constructor.
   *
   * @param learningGoalsService the injected learning goal service
   * @param activeModal the injected active modal service
   * @param accountService the injected account service
   * @param courseService the injected course service
   * @param modalService the injected modal service
   */
  constructor(
    private learningGoalsService: LearningGoalsService,
    private activeModal: NgbActiveModal,
    private accountService: AccountService,
    private courseService: CourseManagementService,
    private modalService: NgbModal
  ) {
    this._loginName = this.accountService.getLoginName()!;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    if (this.useOnlyCourseGoals) {
      this.courseService.getLearningGoalsFromCourse(this.courseName, this._loginName).subscribe(value => {
        this.allLearningGoals = value;
        if (this.filterGoalTrees) {
          this.filteredGoals = this.allLearningGoals.filter(g => this.containsAssignedGoalInHierarchy(g));
        } else {
          this.filteredGoals = this.allLearningGoals;
        }
      });
    } else {
      this.learningGoalsService.getAllVisibleLearningGoalsAsTreeViewItems(this._loginName).subscribe(value => {
        this.allLearningGoals = value;
        if (this.filterGoalTrees) {
          this.filteredGoals = this.allLearningGoals.filter(g => this.containsAssignedGoalInHierarchy(g));
        } else {
          this.filteredGoals = this.allLearningGoals;
        }
      });
    }
  }

  /**
   * Recursively checks if any of the sub-goals of the passed goals is included in the assigned goals
   * Thereby, only relevant hierarchies are displayed
   * @param goal
   */
  public containsAssignedGoalInHierarchy(goal: LearningGoalTreeviewItem): boolean {
    if (this._assignedGoals.includes(goal.text)) {
      return true;
    }

    if (goal.childItems.length > 0) {
      for (const child of goal.childItems) {
        if (this.containsAssignedGoalInHierarchy(child)) {
          return true;
        }
      }
    }

    return false;
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

  /**
   * Closes the current modal window.
   */
  public close(): void {
    this.activeModal.close();
  }

  /**
   * Handles a click on a goal by displaying the description of the goal in a modal window
   * @param item
   */
  public handleGoalClicked(item: LearningGoalTreeviewItem): void {
    const description = item.description;
    if (description) {
      const modalRef = this.modalService.open(LearningGoalDescriptionModalComponent, { backdrop: 'static', size: 'xl' });
      (modalRef.componentInstance as LearningGoalDescriptionModalComponent).description = description;
      (modalRef.componentInstance as LearningGoalDescriptionModalComponent).name = item.text;
    }
  }
}
