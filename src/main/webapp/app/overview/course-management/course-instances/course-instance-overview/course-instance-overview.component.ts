import { Component, OnDestroy, OnInit } from '@angular/core';
import { CourseManagementService } from '../../course-management.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ITEMS_PER_PAGE } from '../../../../shared/constants/pagination.constants';
import { IDisplayableCourseInstanceDTO, Term } from '../../course-mangement.model';
import { Subscription } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { StudentAssignmentModalComponent } from './student-assignment-modal/student-assignment-modal.component';
import { JhiEventManager } from 'ng-jhipster';

/**
 * Component for displaying instances from a course.
 */
@Component({
  selector: 'jhi-course-instance-overview',
  templateUrl: './course-instance-overview.component.html',
  styleUrls: ['./course-instance-overview.component.scss'],
})
export class CourseInstanceOverviewComponent implements OnInit, OnDestroy {
  private _courseName?: string;
  private routingSubscription?: Subscription;
  private studentAssignmentChangedSubscription?: Subscription;

  public page = 1;
  public itemsPerPage: number;
  public totalItems = 0;
  public items: IDisplayableCourseInstanceDTO[] = [];

  /**
   * Constructor.
   *
   * @param courseService the injected course service
   * @param router the injected routing service
   * @param activatedRoute the injected activated route service
   * @param modalService the injected modal service
   * @param eventManager the injected jhi event manager
   */
  constructor(
    private courseService: CourseManagementService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private eventManager: JhiEventManager
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
  }

  /**
   * Implements the init method. See {@code OnInit}.
   */
  public ngOnInit(): void {
    this.routingSubscription = this.activatedRoute.paramMap.subscribe(paramMap => (this.courseName = paramMap.get('courseName')!));
    this.studentAssignmentChangedSubscription = this.eventManager.subscribe('course-instance-stud-assignment-changed', () =>
      this.loadPageAsync()
    );
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

    this.totalItems = Number(response.headers.get('X-Total-Count'));
    this.items = response.body ?? [];
  }
}
