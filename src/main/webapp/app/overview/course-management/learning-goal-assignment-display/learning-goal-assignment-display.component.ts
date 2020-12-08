import { Component, Input, OnInit } from '@angular/core';
import { ICourseModel } from '../course-mangement.model';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CourseManagementService } from '../course-management.service';
import { AccountService } from '../../../core/auth/account.service';
import { LearningGoalTreeviewItem } from '../../learning-goals/learning-goal-treeview-item.model';
import { TreeviewConfig, TreeviewI18n } from 'ngx-treeview';
import { DefaultTreeviewI18n } from '../../../shared/util/default-treeview-i18n';

/**
 * Component which is used to display a course's assigned goals.
 */
@Component({
  selector: 'jhi-learning-goal-assignment-display',
  templateUrl: './learning-goal-assignment-display.component.html',
  styleUrls: ['./learning-goal-assignment-display.component.scss'],
  providers: [{ provide: TreeviewI18n, useClass: DefaultTreeviewI18n }],
})
export class LearningGoalAssignmentDisplayComponent implements OnInit {
  private _selectedCourse?: ICourseModel;
  private loginName = '';

  public goals: LearningGoalTreeviewItem[] = [];
  public config = TreeviewConfig.create({
    hasAllCheckBox: false,
    hasFilter: true,
    hasCollapseExpand: true,
  });

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
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}

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
