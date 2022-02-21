import { Component, OnInit } from '@angular/core';
import { StudentService } from '../shared/students/student-service';
import { ICourseInstanceInformationDTO } from '../shared/students/students.model';
import { Term } from '../course-management/course-mangement.model';
import { Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { StudentExerciseSheetGoalsComponent } from './course-task-overview/student-exercise-sheet-tasks/student-exercise-sheet-goals/student-exercise-sheet-goals.component';
import { AccountService } from '../../core/auth/account.service';

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
  public loginName = '';

  /**
   * Constructor.
   *
   * @param studentService the injected student service
   * @param router the injected routing service
   * @param modalService the injected modal service
   */
  constructor(
    private studentService: StudentService,
    private router: Router,
    private modalService: NgbModal,
    private accountService: AccountService
  ) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.studentService.getCourseInstancesOfLoggedInStudent().subscribe(values => {
      this.courses = values;
    });
    this.loginName = this.accountService.getLoginName()!;
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
    const modalRef = this.modalService.open(StudentExerciseSheetGoalsComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).filterGoalTrees = false;
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).assignedGoals = [];
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).header = courseInstance.courseName;
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).courseName = courseInstance.courseName;
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).useOnlyCourseGoals = true;
  }

  /**
   * Displays the reached goals
   * @param course the course
   */
  public viewReachedGoals(course: ICourseInstanceInformationDTO): void {
    this.viewReachedGoalsAsync(course);
  }

  /**
   * Asynchronously opens the modal window displaying the reached goals as treeview
   * @param course the course
   * @private
   */
  private async viewReachedGoalsAsync(course: ICourseInstanceInformationDTO): Promise<any> {
    const reachedGoalsResponse = await this.studentService.getReachedGoalsOfCourseInstance(course.instanceId).toPromise();
    const reachedGoalsUnfiltered = reachedGoalsResponse.body ?? [];
    const reachedGoals = reachedGoalsUnfiltered.map(g => g.substr(g.lastIndexOf('#') + 1));
    const modalRef = this.modalService.open(StudentExerciseSheetGoalsComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).filterGoalTrees = true;
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).assignedGoals = reachedGoals;
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).header = this.loginName;
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).courseName = course.courseName;
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).useOnlyCourseGoals = true;
  }
}
