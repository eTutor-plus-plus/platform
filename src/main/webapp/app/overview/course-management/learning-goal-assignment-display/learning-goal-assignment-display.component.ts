import { Component, Input } from '@angular/core';
import { ICourseModel } from '../course-mangement.model';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CourseManagementService } from '../course-management.service';
import { AccountService } from '../../../core/auth/account.service';
import { LearningGoalTreeviewItem } from '../../shared/learning-goal-treeview-item.model';

/**
 * Component which is used to display a course's assigned goals.
 */
@Component({
  selector: 'jhi-learning-goal-assignment-display',
  templateUrl: './learning-goal-assignment-display.component.html',
  styleUrls: ['./learning-goal-assignment-display.component.scss'],
})
export class LearningGoalAssignmentDisplayComponent {
  public goals: LearningGoalTreeviewItem[] = [];

  private _selectedCourse?: ICourseModel;
  private loginName = '';

  /**
   * Constructor.
   *
   * @param activeModal the injected active modal service
   * @param courseManagementService the injected course management service
   * @param accountService the injected account service
   */
  constructor(
    private activeModal: NgbActiveModal,
    private courseManagementService: CourseManagementService,
    private accountService: AccountService
  ) {
    this.loginName = this.accountService.getLoginName()!;
  }

  /**
   * Closes the current modal window.
   */
  public close(): void {
    this.activeModal.close();
  }

  /**
   * Sets the selected course.
   *
   * @param value the course value to set
   */
  @Input()
  public set selectedCourse(value: ICourseModel) {
    this.courseManagementService.getLearningGoalsFromCourse(value, this.loginName).subscribe(x => (this.goals = x));
    this._selectedCourse = value;
  }

  /**
   * Returns the selected course.
   */
  public get selectedCourse(): ICourseModel {
    return this._selectedCourse!;
  }
}
