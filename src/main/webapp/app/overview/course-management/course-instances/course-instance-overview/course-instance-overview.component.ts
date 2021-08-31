import { Component, OnDestroy, OnInit } from '@angular/core';
import { CourseManagementService } from '../../course-management.service';
import { ActivatedRoute, Router } from '@angular/router';
import { IDisplayableCourseInstanceDTO, Term } from '../../course-mangement.model';
import { Subscription } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { StudentAssignmentModalComponent } from '../../../course-management-shared/student-assignment-modal/student-assignment-modal.component';
import { CourseExerciseSheetAllocationComponent } from '../../../course-management-shared/course-exercise-sheet-allocation/course-exercise-sheet-allocation.component';
import { TranslatePipe } from '@ngx-translate/core';
import { COUNT_HEADER, ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { AlertService } from 'app/core/util/alert.service';
import { EventManager } from 'app/core/util/event-manager.service';

/**
 * Component for displaying instances from a course.
 */
@Component({
  selector: 'jhi-course-instance-overview',
  templateUrl: './course-instance-overview.component.html',
  styleUrls: ['./course-instance-overview.component.scss'],
  providers: [TranslatePipe],
})
export class CourseInstanceOverviewComponent implements OnInit, OnDestroy {
  public page = 1;
  public readonly itemsPerPage: number;
  public totalItems = 0;
  public items: IDisplayableCourseInstanceDTO[] = [];

  public deletePopoverTitle = 'courseManagement.instances.overview.deletePopover.title';
  public deletePopoverMessage = 'courseManagement.instances.overview.deletePopover.message';
  public deletePopoverCancelBtnText = 'courseManagement.instances.overview.deletePopover.cancelBtn';
  public deletePopoverConfirmBtnText = 'courseManagement.instances.overview.deletePopover.confirmBtn';

  private _courseName?: string;
  private routingSubscription?: Subscription;
  private studentAssignmentChangedSubscription?: Subscription;

  /**
   * Constructor.
   *
   * @param courseService the injected course service
   * @param router the injected routing service
   * @param activatedRoute the injected activated route service
   * @param modalService the injected modal service
   * @param eventManager the injected jhi event manager
   * @param alertService the injected alert service
   * @param translatePipe the injected translation pipe
   */
  constructor(
    private courseService: CourseManagementService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private eventManager: EventManager,
    private alertService: AlertService,
    private translatePipe: TranslatePipe
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
  }

  /**
   * Implements the init method. See {@code OnInit}.
   */
  public ngOnInit(): void {
    this.deletePopoverTitle = this.translatePipe.transform(this.deletePopoverTitle);
    this.deletePopoverMessage = this.translatePipe.transform(this.deletePopoverMessage);
    this.deletePopoverCancelBtnText = this.translatePipe.transform(this.deletePopoverCancelBtnText);
    this.deletePopoverConfirmBtnText = this.translatePipe.transform(this.deletePopoverConfirmBtnText);

    this.routingSubscription = this.activatedRoute.paramMap.subscribe(paramMap => (this.courseName = paramMap.get('courseName')!));
    this.studentAssignmentChangedSubscription = this.eventManager.subscribe('course-instance-stud-assignment-changed', () => {
      this.loadPageAsync();
    });
  }

  /**
   * Implements the destroy method. See {@code OnDestroy}.
   */
  public ngOnDestroy(): void {
    if (this.routingSubscription) {
      this.routingSubscription.unsubscribe();
    }
    if (this.studentAssignmentChangedSubscription) {
      this.studentAssignmentChangedSubscription.unsubscribe();
    }
  }

  /**
   * Sets the course name.
   *
   * @param value the course name value to set
   */
  public set courseName(value: string) {
    this._courseName = value;

    this.loadPageAsync();
  }

  /**
   * Returns the course name.
   */
  public get courseName(): string {
    return this._courseName!;
  }

  /**
   * Returns the identity id from the given item.
   *
   * @param index the index
   * @param item the item
   */
  public trackIdentity(index: number, item: IDisplayableCourseInstanceDTO): string {
    return item.id;
  }

  /**
   * Loads the currently selected page.
   */
  public transition(): void {
    this.loadPageAsync();
  }

  /**
   * Returns the i18n constant for the given term url.
   *
   * @param url the term url
   */
  public getTermI18NStringFromURL(url: string): string {
    return Term.fromString(url)!.text;
  }

  /**
   * Navigates back to the course overview.
   */
  public navigateBack(): void {
    this.router.navigate(['../../../'], {
      relativeTo: this.activatedRoute,
    });
  }

  /**
   * Opens the student assignment window for the given course instance.
   *
   * @param item the current course instance
   */
  public showAssignStudentsModalWindow(item: IDisplayableCourseInstanceDTO): void {
    const modalRef = this.modalService.open(StudentAssignmentModalComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as StudentAssignmentModalComponent).courseInstance = item;
  }

  /**
   * Opens the exercise sheet assignment window for the given course instance.
   *
   * @param item the current course instance
   */
  public showAssignExerciseSheetModalWindow(item: IDisplayableCourseInstanceDTO): void {
    const modalRef = this.modalService.open(CourseExerciseSheetAllocationComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as CourseExerciseSheetAllocationComponent).courseInstance = item;
  }

  /**
   * Deletes the given course instance.
   *
   * @param item the current course instance
   */
  public deleteCourseInstance(item: IDisplayableCourseInstanceDTO): void {
    (async () => {
      await this.courseService.deleteCourseInstance(item.id).toPromise();
      const newItemLength = this.totalItems - 1;
      if (newItemLength > 0 && newItemLength % this.itemsPerPage === 0) {
        this.page -= 1;
      }

      await this.loadPageAsync();
      this.alertService.addAlert({
        type: 'success',
        translationKey: 'courseManagement.instances.overview.courseInstanceRemoved',
        translationParams: { name: item.name },
        timeout: 5000,
      });
    })();
  }

  /**
   * Asynchronously loads the page.
   */
  private async loadPageAsync(): Promise<any> {
    const response = await this.courseService
      .getOverviewInstances(this.courseName, {
        page: this.page - 1,
        size: this.itemsPerPage,
        sort: [],
      })
      .toPromise();

    this.totalItems = Number(response.headers.get(COUNT_HEADER));
    this.items = response.body ?? [];
  }
}
