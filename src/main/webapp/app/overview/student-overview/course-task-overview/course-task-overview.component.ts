import { Component, OnInit } from '@angular/core';
import { StudentService } from '../../shared/students/student-service';

/**
 * Component for displaying the student's task of a given course instance.
 */
@Component({
  selector: 'jhi-course-task-overview',
  templateUrl: './course-task-overview.component.html',
  styleUrls: ['./course-task-overview.component.scss'],
})
export class CourseTaskOverviewComponent implements OnInit {
  constructor(private studentService: StudentService) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}
}
