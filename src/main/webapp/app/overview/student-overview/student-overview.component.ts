import { Component, OnInit } from '@angular/core';
import { StudentService } from '../shared/students/student-service';
import { ICourseInstanceInformationDTO } from '../shared/students/students.model';
import { ICourseModel, Term } from '../course-management/course-mangement.model';
import { Router } from '@angular/router';
import { LearningGoalAssignmentDisplayComponent } from '../course-management/learning-goal-assignment-display/learning-goal-assignment-display.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

// noinspection JSIgnoredPromiseFromCall
/**
 * Component for displaying a student's overview page.
 */
@Component({
  selector: 'jhi-student-overview',
  templateUrl: './student-overview.component.html',
  styleUrls: ['./student-overview.component.scss'],
})
export class StudentOverviewComponent implements OnInit {
  public courses: ICourseInstanceInformationDTO[] = [];

  /**
   * Constructor.
   *
   * @param studentService the injected student service
   * @param router the injected routing service
   * @param modalService the injected modal service
   */
  constructor(private studentService: StudentService, private router: Router, private modalService: NgbModal) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.studentService.getCourseInstancesOfLoggedInStudent().subscribe(values => {
      this.courses = values;
    });
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
  public navigateToCourseAssignments(course: ICourseInstanceInformationDTO): void {
    this.router.navigate(['/overview/student/exercises'], { state: { instance: course } });
  }

  /**
   * Navigates to the course's self assessment page.
   *
   * @param course the corresponding course model
   */
  public navigateToCourseSelfAssessment(course: ICourseInstanceInformationDTO): void {
    this.router.navigate(['/overview/student/self-assessment'], { state: { instance: course } });
  }

  /**
   * Opens a modal window that displays the assigned learning goals of the course
   * @param courseInstance the course instance
   */
  public viewGoalAssignments(courseInstance: ICourseInstanceInformationDTO): void {
    const modalRef = this.modalService.open(LearningGoalAssignmentDisplayComponent, { size: 'lg', backdrop: 'static' });
    const course: ICourseModel = {
      courseType: '',
      instanceCount: 0,
      name: courseInstance.courseName,
    };
    (modalRef.componentInstance as LearningGoalAssignmentDisplayComponent).selectedCourse = course;
  }
}
