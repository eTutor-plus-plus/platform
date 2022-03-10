import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { StudentService } from '../../shared/students/student-service';
import { ActivatedRoute, Router } from '@angular/router';
import { ICourseInstanceInformationDTO, ICourseInstanceProgressOverviewDTO } from '../../shared/students/students.model';
import { TaskDifficulty } from '../../tasks/task.model';
import { StudentExerciseSheetGoalsComponent } from './student-exercise-sheet-tasks/student-exercise-sheet-goals/student-exercise-sheet-goals.component';
import { ExerciseSheetsService } from '../../exercise-sheets/exercise-sheets.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

// noinspection JSIgnoredPromiseFromCall
/**
 * Component for displaying the student's task of a given course instance.
 */
@Component({
  selector: 'jhi-course-task-overview',
  templateUrl: './course-task-overview.component.html',
  styleUrls: ['./course-task-overview.component.scss'],
})
export class CourseTaskOverviewComponent implements OnInit {
  public instance?: ICourseInstanceInformationDTO;
  public items: ICourseInstanceProgressOverviewDTO[] = [];

  /**
   * Constructor.
   *
   * @param studentService the injected student service
   * @param router the injected router
   * @param location the injected location service
   * @param activatedRoute the injected activated route
   * @param exerciseSheetService the injected exercise sheet service
   * @param modalService
   */
  constructor(
    private studentService: StudentService,
    private router: Router,
    private location: Location,
    private activatedRoute: ActivatedRoute,
    private exerciseSheetService: ExerciseSheetsService,
    private modalService: NgbModal
  ) {
    const nav = this.router.getCurrentNavigation();

    if (nav?.extras.state) {
      this.instance = nav.extras.state.instance;
    } else {
      this.router.navigate(['/']);
    }
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    if (this.instance) {
      this.studentService.getStudentCourseInstanceProgressOverview(this.instance.instanceId).subscribe(value => {
        this.items = value;
      });
    }
  }

  /**
   * Returns the i18n string of a given difficulty url.
   *
   * @param url the url
   */
  public getDifficultyI18nString(url: string): string {
    return TaskDifficulty.fromString(url)!.text;
  }

  /**
   * Tracks the identity of the given item.
   *
   * @param index the index
   * @param item the item
   */
  public trackIdentity(index: number, item: ICourseInstanceProgressOverviewDTO): string {
    return item.exerciseSheetId;
  }

  /**
   * Navigates back.
   */
  public navigateBack(): void {
    this.location.back();
  }

  /**
   * Navigates to the tasks of the given exercise sheet.
   *
   * @param item the progress info
   */
  public navigateToTasks(item: ICourseInstanceProgressOverviewDTO): void {
    const exerciseSheetUUID = item.exerciseSheetId.substr(item.exerciseSheetId.lastIndexOf('#') + 1);

    if (!item.opened) {
      this.studentService.openExerciseSheet(this.instance!.instanceId, exerciseSheetUUID).subscribe(() => {
        this.navigateToTaskOverview(exerciseSheetUUID, item.closed);
      });
    } else {
      if (item.submissionCount === item.gradedCount && item.submissionCount > 0 && item.actualCount === item.submissionCount) {
        (async () => {
          const result = await this.studentService.canAssignNextTask(this.instance!.instanceId, exerciseSheetUUID).toPromise();

          if (result) {
            const isNewTaskAssigned = await this.studentService.assignNewTask(this.instance!.instanceId, exerciseSheetUUID).toPromise();

            if (!isNewTaskAssigned) {
              item.closed = true;
            }
          }
          this.navigateToTaskOverview(exerciseSheetUUID, item.closed);
        })();
      } else {
        this.navigateToTaskOverview(exerciseSheetUUID, item.closed);
      }
    }
  }

  public viewGoalAssignments(item: ICourseInstanceProgressOverviewDTO): void {
    this.viewGoalAssignmentsAsync(item);
  }
  /**
   * Navigates to the task overview.
   *
   * @param exerciseSheetUUID the exercise sheet uuid
   * @param sheetAlreadyClosed indicates whether the
   * exercise sheet has already been closed or not
   */
  private navigateToTaskOverview(exerciseSheetUUID: string, sheetAlreadyClosed: boolean): void {
    this.router.navigate([exerciseSheetUUID, 'tasks'], {
      relativeTo: this.activatedRoute,
      state: { instance: this.instance, closed: sheetAlreadyClosed },
    });
  }

  /**
   * Opens the modal window to view the assigned goals of an exercise sheet
   * @param item the course instance
   * @private
   */
  private async viewGoalAssignmentsAsync(item: ICourseInstanceProgressOverviewDTO): Promise<any> {
    const exerciseSheetResponse = await this.exerciseSheetService
      .getExerciseSheetById(item.exerciseSheetId.substr(item.exerciseSheetId.lastIndexOf('#') + 1))
      .toPromise();
    const exerciseSheet = exerciseSheetResponse.body!;
    const assignedGoalsOfSheet = exerciseSheet.learningGoals.filter(g => g.learningGoal.name).map(g => g.learningGoal.name!);
    const modalRef = this.modalService.open(StudentExerciseSheetGoalsComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).assignedGoals = assignedGoalsOfSheet;
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).header = item.assignmentHeader;
    if (this.instance?.courseName) {
      (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).courseName = this.instance.courseName!;
      (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).useOnlyCourseGoals = true;
    }
  }
}
