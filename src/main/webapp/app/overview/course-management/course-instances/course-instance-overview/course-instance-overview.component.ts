import { Component, OnInit } from '@angular/core';
import { CourseManagementService } from '../../course-management.service';
import { ActivatedRoute, Router } from '@angular/router';

/**
 * Component for displaying instances from a course.
 */
@Component({
  selector: 'jhi-course-instance-overview',
  templateUrl: './course-instance-overview.component.html',
  styleUrls: ['./course-instance-overview.component.scss'],
})
export class CourseInstanceOverviewComponent implements OnInit {
  /**
   * Constructor.
   *
   * @param courseService the injected course service
   * @param router the injected routing service
   * @param activatedRoute the injected activated route service
   */
  constructor(private courseService: CourseManagementService, private router: Router, private activatedRoute: ActivatedRoute) {}

  /**
   * Implements the init method. See {@code OnInit}.
   */
  public ngOnInit(): void {}
}
