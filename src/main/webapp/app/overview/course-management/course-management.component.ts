import { Component, OnDestroy, OnInit } from '@angular/core';
import { AccountService } from '../../core/auth/account.service';
import { CourseModel } from './course-mangement.model';
import { CourseManagementService } from './course-management.service';
import { JhiEventManager } from 'ng-jhipster';
import { Subscription } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { UpdateCourseComponent } from './update-course/update-course.component';

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
  private username!: string;
  private subscription?: Subscription;

  public popoverTitle = 'courseManagement.popover.title';
  public popoverMessage = 'courseManagement.popover.message';
  public popoverCancelBtnText = 'courseManagement.popover.cancelBtn';
  public popoverConfirmBtnText = 'courseManagement.popover.confirmBtn';

  public courses: CourseModel[] = [];

  /**
   * Constructor.
   *
   * @param accountService the injected account service
   * @param courseService the injected course service
   * @param eventManager the injected event manager
   * @param translatePipe the injected translate pipe
   * @param modalService the injected modal service
   */
  constructor(
    private accountService: AccountService,
    private courseService: CourseManagementService,
    private eventManager: JhiEventManager,
    private translatePipe: TranslatePipe,
    private modalService: NgbModal
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
  }

  /**
   * Shows the create / edit window for the given course
   *
   * @param course the course to edit
   */
  public editCourse(course: CourseModel): void {
    const modalRef = this.modalService.open(UpdateCourseComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as UpdateCourseComponent).course = course;
  }

  /**
   * Deletes the course
   *
   * @param course the course to delete
   */
  public deleteCourse(course: CourseModel): void {
    this.courseService.deleteCourse(course).subscribe(() => {
      this.eventManager.broadcast('courseChanged');
    });
  }

  /**
   * Loads the courses asynchronously from the service.
   */
  private async loadCourses(): Promise<any> {
    this.courses = await this.courseService.getAllCourses().toPromise();
  }
}
