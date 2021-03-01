import { Component, OnInit } from '@angular/core';
import { StudentService } from '../shared/students/student-service';
import { CourseInstanceInformationDTO } from '../shared/students/students.model';
import { Term } from '../course-management/course-mangement.model';
import { Router } from '@angular/router';

/**
 * Component for displaying a student's overview page.
 */
@Component({
  selector: 'jhi-student-overview',
  templateUrl: './student-overview.component.html',
  styleUrls: ['./student-overview.component.scss'],
})
export class StudentOverviewComponent implements OnInit {
  public courses: CourseInstanceInformationDTO[] = [];

  /**
   * Constructor.
   *
   * @param studentService the injected student service
   * @param router the injected routing service
   */
  constructor(private studentService: StudentService, private router: Router) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.studentService.getCourseInstancesOfLoggedInStudent().subscribe(values => (this.courses = values));
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
   * Navigates to the given course instance's assignments.
   *
   * @param course the course instance
   */
  public navigateToCourseAssignments(course: CourseInstanceInformationDTO): void {}
}
