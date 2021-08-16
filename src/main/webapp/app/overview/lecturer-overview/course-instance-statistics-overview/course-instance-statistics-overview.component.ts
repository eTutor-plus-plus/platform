// noinspection JSIgnoredPromiseFromCall

import { Component, OnDestroy, OnInit } from '@angular/core';
import { LecturerOverviewService } from '../lecturer-overview.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { IStatisticsOverviewModelDTO } from '../lecturer-overview.model';
import { NgxSpinnerService } from 'ngx-spinner';

/**
 * Component which is used for displaying a statistical overview
 * of a chosen course instance.
 */
@Component({
  selector: 'jhi-course-instance-statistics-overview',
  templateUrl: './course-instance-statistics-overview.component.html',
  styleUrls: ['./course-instance-statistics-overview.component.scss'],
})
export class CourseInstanceStatisticsOverviewComponent implements OnInit, OnDestroy {
  public statisticsInfo?: IStatisticsOverviewModelDTO;

  private _activatedRouteSubscription?: Subscription;
  private _instanceId = '';

  /**
   * Constructor.
   *
   * @param lecturerOverviewService the injected lecturer overview service
   * @param activatedRoute the injected activated route service
   * @param router the injected routing service
   * @param spinner the injected ngx-spinner service
   */
  constructor(
    private lecturerOverviewService: LecturerOverviewService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private spinner: NgxSpinnerService
  ) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.spinner.show();

    this._activatedRouteSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this._instanceId = paramMap.get('instanceId')!;

      this.lecturerOverviewService.getStatisticalOverviewOfCourseInstance(this._instanceId).subscribe(value => {
        this.statisticsInfo = value;
        this.spinner.hide();
      });
    });
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  public ngOnDestroy(): void {
    this._activatedRouteSubscription?.unsubscribe();
  }

  /**
   * Navigates back to the overview.
   */
  public navigateBackToOverview(): void {
    this.router.navigate(['/', 'overview']);
  }
}
