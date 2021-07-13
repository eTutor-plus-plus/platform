import { Component } from '@angular/core';
import { LecturerOverviewService } from '../lecturer-overview.service';

/**
 * Component which is used for displaying a statistical overview
 * of a chosen course instance.
 */
@Component({
  selector: 'jhi-course-instance-statistics-overview',
  templateUrl: './course-instance-statistics-overview.component.html',
  styleUrls: ['./course-instance-statistics-overview.component.scss'],
})
export class CourseInstanceStatisticsOverviewComponent {
  /**
   * Constructor.
   *
   * @param lecturerOverviewService the injected lecturer overview service
   */
  constructor(private lecturerOverviewService: LecturerOverviewService) {}
}
