// noinspection JSIgnoredPromiseFromCall

import { Component, OnInit } from '@angular/core';
import { LecturerOverviewService } from './lecturer-overview.service';
import { COUNT_HEADER, ITEMS_PER_PAGE } from '../../config/pagination.constants';
import { StudentAssignmentModalComponent } from '../course-management-shared/student-assignment-modal/student-assignment-modal.component';
import { CourseExerciseSheetAllocationComponent } from '../course-management-shared/course-exercise-sheet-allocation/course-exercise-sheet-allocation.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { IDisplayableCourseInstanceDTO } from '../course-management/course-mangement.model';
import { ActivatedRoute, Router } from '@angular/router';

/**
 * Component which is used for displaying an overview for a lecturer.
 */
@Component({
  selector: 'jhi-lecturer-overview',
  templateUrl: './lecturer-overview.component.html',
  styleUrls: ['./lecturer-overview.component.scss'],
})
export class LecturerOverviewComponent implements OnInit {
  public page = 1;
  public readonly itemsPerPage: number;
  public totalItems = 0;
  public courseInstances: IDisplayableCourseInstanceDTO[] = [];

  /**
   * Constructor.
   *
   * @param lecturerOverviewService the injected lecturer overview service
   * @param modalService the injected modal service
   * @param router the injected routing service
   * @param activatedRoute the injected actice route
   */
  constructor(
    private lecturerOverviewService: LecturerOverviewService,
    private modalService: NgbModal,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.loadCoursesAsync();
  }

  /**
   * Performs the transition.
   */
  public transition(): void {
    this.loadCoursesAsync();
  }

  /**
   * Shows the course statistics of the selected course.
   *
   * @param selectedCourse the selected course
   */
  public showStatistics(selectedCourse: IDisplayableCourseInstanceDTO): void {
    const courseInstanceId = selectedCourse.id.substr(selectedCourse.id.lastIndexOf('#') + 1);
    this.router.navigate(['lecturer', 'statistics', courseInstanceId], { relativeTo: this.activatedRoute });
  }

  public showAssignExerciseSheetModalWindow(selectedCourse: IDisplayableCourseInstanceDTO): void {
    const modalRef = this.modalService.open(CourseExerciseSheetAllocationComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as CourseExerciseSheetAllocationComponent).courseInstance = selectedCourse;
  }

  public showAssignStudentsModalWindow(selectedCourse: IDisplayableCourseInstanceDTO): void {
    const modalRef = this.modalService.open(StudentAssignmentModalComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as StudentAssignmentModalComponent).courseInstance = selectedCourse;
  }

  /**
   * Asynchronously loads the courses.
   */
  private async loadCoursesAsync(): Promise<void> {
    const result = await this.lecturerOverviewService
      .getPagedCoursesOfUser({
        page: this.page - 1,
        size: this.itemsPerPage,
        sort: [],
      })
      .toPromise();

    this.totalItems = Number(result.headers.get(COUNT_HEADER));
    this.courseInstances = result.body ?? [];
  }
}
