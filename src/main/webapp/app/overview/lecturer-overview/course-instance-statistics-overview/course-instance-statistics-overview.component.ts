import { Component, OnDestroy, OnInit } from '@angular/core';
import { LecturerOverviewService } from '../lecturer-overview.service';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

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
  private _activatedRouteSubscription?: Subscription;
  private _instanceId = '';

  /**
   * Constructor.
   *
   * @param lecturerOverviewService the injected lecturer overview service
   * @param activatedRoute the injected activated route service
   */
  constructor(private lecturerOverviewService: LecturerOverviewService, private activatedRoute: ActivatedRoute) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this._activatedRouteSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this._instanceId = paramMap.get('instanceId')!;
    });
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  public ngOnDestroy(): void {
    this._activatedRouteSubscription?.unsubscribe();
  }
}
