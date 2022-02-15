import { Component, OnDestroy, OnInit } from '@angular/core';
import { StudentService } from '../../../shared/students/student-service';
import { ActivatedRoute, Router } from '@angular/router';
import { ICourseInstanceInformationDTO, IStudentTaskListInfoDTO } from '../../../shared/students/students.model';
import { Subscription } from 'rxjs';
import { Location } from '@angular/common';
import { ExerciseSheetsService } from 'app/overview/exercise-sheets/exercise-sheets.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { StudentExerciseSheetGoalsComponent } from './student-exercise-sheet-goals/student-exercise-sheet-goals.component';
import { TasksService } from '../../../tasks/tasks.service';

// noinspection JSIgnoredPromiseFromCall
/**
 * Component which is used for displaying the individual tasks
 * of a student's exercise sheet.
 */
@Component({
  selector: 'jhi-student-exercise-sheet-tasks',
  templateUrl: './student-exercise-sheet-tasks.component.html',
  styleUrls: ['./student-exercise-sheet-tasks.component.scss'],
})
export class StudentExerciseSheetTasksComponent implements OnInit, OnDestroy {
  public entries: IStudentTaskListInfoDTO[] = [];
  public exerciseSheetName = '';
  public exerciseSheetClosed = false;
  public fileAttachmentId = -1;

  private readonly _instance?: ICourseInstanceInformationDTO;
  private _paramMapSubscription?: Subscription;
  private _exerciseSheetUUID = '';

  /**
   * Constructor.
   *
   * @param studentService the injected student service
   * @param router the injected router
   * @param location the injected location service
   * @param activatedRoute the injected activated route
   * @param exerciseSheetService the injected exercise sheet service
   * @param modalService the injected modal service
   * @param taskService the injected task service
   */
  constructor(
    private studentService: StudentService,
    private router: Router,
    private location: Location,
    private activatedRoute: ActivatedRoute,
    private exerciseSheetService: ExerciseSheetsService,
    private modalService: NgbModal,
    private taskService: TasksService
  ) {
    const nav = this.router.getCurrentNavigation();

    if (nav?.extras.state) {
      this._instance = nav.extras.state.instance;
      this.exerciseSheetClosed = nav.extras.state.closed;
    } else {
      this.router.navigate(['/']);
    }
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this._paramMapSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this._exerciseSheetUUID = paramMap.get('exerciseSheetUUID')!;
      (async () => {
        const result = await this.exerciseSheetService.getExerciseSheetById(this._exerciseSheetUUID).toPromise();
        if (result.body) {
          this.exerciseSheetName = result.body.name;
        }
      })();
      this.loadTasksAsync();
      this.loadFileAttachmentAsync();
    });
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  public ngOnDestroy(): void {
    this._paramMapSubscription?.unsubscribe();
  }

  /**
   * Navigates back.
   */
  public navigateBack(): void {
    this.location.back();
  }

  /**
   * Opens the given task entry.
   *
   * @param taskEntry the task entry to open
   */
  public openTask(taskEntry: IStudentTaskListInfoDTO): void {
    const taskUUID = taskEntry.taskId.substr(taskEntry.taskId.lastIndexOf('#') + 1);
    this.router.navigate(['task', taskUUID, 'taskNo', taskEntry.orderNo], {
      relativeTo: this.activatedRoute,
      state: { instance: this._instance, closed: this.exerciseSheetClosed },
    });
  }

  /**
   * Opens the assigned learning goals for the exercise sheet as treeview-component for the exercise sheet
   */
  public openLearningGoalsForSheet(): void {
    this.openLearningGoalsForSheetAsync();
  }

  /**
   * Opens the assigned learning goals of a task assignment
   * @param entry
   */
  public viewGoalAssignments(entry: IStudentTaskListInfoDTO): void {
    this.viewGoalAssignmentsAsync(entry);
  }

  /**
   * Asynchronously loads the student's tasks.
   */
  private async loadTasksAsync(): Promise<any> {
    const result = await this.studentService.getExerciseSheetTasks(this._instance!.instanceId, this._exerciseSheetUUID).toPromise();
    this.entries = result.body ?? [];
  }

  /**
   * Asynchronously loads the file attachment id of the individual assignment.
   * @private
   */
  private async loadFileAttachmentAsync(): Promise<any> {
    this.fileAttachmentId = await this.studentService
      .getFileAttachmentIdForExerciseSheet(this._instance!.instanceId, this._exerciseSheetUUID)
      .toPromise();
  }

  /**
   * Opens the modal window that displays the assigned learning goals of the exercise sheet
   * @private
   */
  private async openLearningGoalsForSheetAsync(): Promise<any> {
    const exerciseSheetResponse = await this.exerciseSheetService.getExerciseSheetById(this._exerciseSheetUUID).toPromise();
    const exerciseSheet = exerciseSheetResponse.body!;
    const assignedGoalsOfSheet = exerciseSheet.learningGoals.filter(g => g.learningGoal.name).map(g => g.learningGoal.name!);
    const modalRef = this.modalService.open(StudentExerciseSheetGoalsComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).assignedGoals = assignedGoalsOfSheet;
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).header = this.exerciseSheetName;
    if (this._instance?.courseName) {
      (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).courseName = this._instance.courseName!;
      (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).useOnlyCourseGoals = true;
    }
  }

  /**
   * Opens the modal window that displays the assigned learning goals of a task assignment
   * @param entry the info about the task
   * @private
   */
  private async viewGoalAssignmentsAsync(entry: IStudentTaskListInfoDTO): Promise<any> {
    const assignedGoalsResponse = await this.taskService.getAssignedLearningGoalsOfAssignment(entry.taskId).toPromise();
    const assignedGoalIds = assignedGoalsResponse.body;
    const assignedGoals = assignedGoalIds?.map(t => t.substr(t.lastIndexOf('#') + 1));
    const modalRef = this.modalService.open(StudentExerciseSheetGoalsComponent, { backdrop: 'static', size: 'xl' });
    if (assignedGoals) {
      (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).assignedGoals = assignedGoals;
    }
    (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).header = entry.taskHeader;
    if (this._instance?.courseName) {
      (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).courseName = this._instance.courseName!;
      (modalRef.componentInstance as StudentExerciseSheetGoalsComponent).useOnlyCourseGoals = true;
    }
  }
}
