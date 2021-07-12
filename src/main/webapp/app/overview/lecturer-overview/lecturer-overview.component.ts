// noinspection JSIgnoredPromiseFromCall

import { Component, OnInit } from '@angular/core';
import { LecturerOverviewService } from './lecturer-overview.service';
import { COUNT_HEADER, ITEMS_PER_PAGE } from '../../config/pagination.constants';
import { ICourseOverviewModel } from './lecturer-overview.model';

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
  public courses: ICourseOverviewModel[] = [];

  /**
   * Constructor.
   *
   * @param lecturerOverviewService the injected lecturer overview service
   */
  constructor(private lecturerOverviewService: LecturerOverviewService) {
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
  public showStatistics(selectedCourse: ICourseOverviewModel): void {
    //TODO: Open new page.
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
    this.courses = result.body ?? [];
  }
}
