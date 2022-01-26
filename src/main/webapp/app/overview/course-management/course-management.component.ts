import { Component, OnDestroy, OnInit } from '@angular/core';
import { AccountService } from 'app/core/auth/account.service';
import { CourseModel } from './course-mangement.model';
import { CourseManagementService } from './course-management.service';
import { Subscription } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { UpdateCourseComponent } from './update-course/update-course.component';
import { ViewCourseComponent } from './view-course/view-course.component';
import { filter } from 'lodash';
import { LearningGoalAssignmentUpdateComponent } from './learning-goal-assignment-update/learning-goal-assignment-update.component';
import { LearningGoalAssignmentDisplayComponent } from './learning-goal-assignment-display/learning-goal-assignment-display.component';
import { CourseInstanceCreationComponent } from './course-instances/course-instance-creation/course-instance-creation.component';
import { ActivatedRoute, Router } from '@angular/router';
import { EventManager } from 'app/core/util/event-manager.service';

// noinspection JSIgnoredPromiseFromCall
/**
 * Component which is used to display the course management
 * overview page.
 */
@Component({
  selector: 'jhi-course-management',
  templateUrl: './course-management.component.html',
  styleUrls: ['./course-management.component.scss'],
  providers: [TranslatePipe],
})
export class CourseManagementComponent implements OnInit, OnDestroy {
  public popoverTitle = 'courseManagement.popover.title';
  public popoverMessage = 'courseManagement.popover.message';
  public popoverCancelBtnText = 'courseManagement.popover.cancelBtn';
  public popoverConfirmBtnText = 'courseManagement.popover.confirmBtn';

  public courses: CourseModel[] = [];

  public filteredCourses: CourseModel[] = [];

  public readonly courseTypes: string[] = ['Fach', 'Klasse', 'LVA', 'Modul'];

  public selectedCourseTypes = [...this.courseTypes];

  public filterString = '';

  private username!: string;
  private subscription?: Subscription;

  /**
   * Constructor.
   *
   * @param accountService the injected account service
   * @param courseService the injected course service
   * @param eventManager the injected event manager
   * @param translatePipe the injected translate pipe
   * @param modalService the injected modal service
   * @param router the injected routing service
   * @param activatedRoute the injected activated route
   */
  constructor(
    private accountService: AccountService,
    private courseService: CourseManagementService,
    private eventManager: EventManager,
    private translatePipe: TranslatePipe,
    private modalService: NgbModal,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.popoverTitle = this.translatePipe.transform(this.popoverTitle);
    this.popoverMessage = this.translatePipe.transform(this.popoverMessage);
    this.popoverCancelBtnText = this.translatePipe.transform(this.popoverCancelBtnText);
    this.popoverConfirmBtnText = this.translatePipe.transform(this.popoverConfirmBtnText);

    this.username = this.accountService.getLoginName() ?? '';
    this.subscription = this.eventManager.subscribe('courseChanged', () => {
      this.loadCourses();
    });
    this.loadCourses();
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  public ngOnDestroy(): void {
    if (this.subscription) {
      this.eventManager.destroy(this.subscription);
      this.subscription = undefined;
    }
  }

  /**
   * Checks whether the given user login is allowed to edit a course
   *
   * @param userLogin the user login to check
   */
  public isUserAllowedToEdit(userLogin: string): boolean {
    return this.username === userLogin;
  }

  /**
   * Track identity function for a course model.
   *
   * @param index the index of the item
   * @param item the actual item
   */
  public trackIdentity(index: number, item: CourseModel): string {
    return item.id!;
  }

  /**
   * Requests the creation of a new course
   */
  public createNewCourse(): void {
    const modalRef = this.modalService.open(UpdateCourseComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as UpdateCourseComponent).course = undefined;
    (modalRef.componentInstance as UpdateCourseComponent).courseTypes = this.courseTypes;
  }

  /**
   * Shows the create / edit window for the given course.
   *
   * @param course the course to edit
   */
  public editCourse(course: CourseModel): void {
    const modalRef = this.modalService.open(UpdateCourseComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as UpdateCourseComponent).courseTypes = this.courseTypes;
    (modalRef.componentInstance as UpdateCourseComponent).course = course;
  }

  /**
   * Deletes the course.
   *
   * @param course the course to delete
   */
  public deleteCourse(course: CourseModel): void {
    this.courseService.deleteCourse(course).subscribe(() => {
      this.eventManager.broadcast('courseChanged');
    });
  }

  /**
   * Displays the selected course.
   *
   * @param course the course to display
   */
  public viewCourse(course: CourseModel): void {
    const modalRef = this.modalService.open(ViewCourseComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as ViewCourseComponent).course = course;
  }

  /**
   * Displays the goal assignment for the given course.
   *
   * @param course the course whose goals should be displayed
   */
  public viewGoalAssignments(course: CourseModel): void {
    const modalRef = this.modalService.open(LearningGoalAssignmentDisplayComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as LearningGoalAssignmentDisplayComponent).selectedCourse = course;
  }

  /**
   * Shows the edit goal assignment windows for the given course.
   *
   * @param course the course whose goals should be edited
   */
  public editGoalAssignments(course: CourseModel): void {
    const modalRef = this.modalService.open(LearningGoalAssignmentUpdateComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as LearningGoalAssignmentUpdateComponent).selectedCourse = course;
  }

  /**
   * Opens the create instance window for the given course.
   *
   * @param course the course
   */
  public createInstance(course: CourseModel): void {
    const modalRef = this.modalService.open(CourseInstanceCreationComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as CourseInstanceCreationComponent).course = course;
    modalRef.closed.subscribe(() => {
      this.loadCourses();
    });
  }

  /**
   * Displays the instances of the given course.
   *
   * @param course the course whose instances should be displayed
   */
  public viewInstances(course: CourseModel): void {
    this.router.navigate(['course-instances', 'of', course.name], {
      relativeTo: this.activatedRoute,
    });
  }

  /**
   * Performs the filtering operation.
   */
  public performFiltering(): void {
    this.filter();
  }

  /**
   * Performs the filtering operation after a course type has been selected.
   */
  public onCourseTypeSelectedChanged(): void {
    this.filter();
  }

  /**
   * Loads the courses asynchronously from the service.
   */
  private async loadCourses(): Promise<any> {
    this.courses = await this.courseService.getAllCourses().toPromise();
    this.performFiltering();
  }

  /**
   * Performs the actual filtering operation.
   */
  private filter(): void {
    this.filteredCourses = filter(this.courses, item => {
      if (this.selectedCourseTypes.includes(item.courseType)) {
        if (this.filterString) {
          return item.name.toLowerCase().includes(this.filterString.toLowerCase());
        }
        return true;
      }
      return false;
    });
  }
}
