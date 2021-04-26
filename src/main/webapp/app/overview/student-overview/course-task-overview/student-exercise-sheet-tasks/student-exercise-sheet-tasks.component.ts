import { Component, OnDestroy, OnInit } from '@angular/core';
import { StudentService } from '../../../shared/students/student-service';
import { ActivatedRoute, Router } from '@angular/router';
import { ICourseInstanceInformationDTO, IStudentTaskListInfoDTO } from '../../../shared/students/students.model';
import { Subscription } from 'rxjs';
import { Location } from '@angular/common';
import { ExerciseSheetsService } from 'app/overview/exercise-sheets/exercise-sheets.service';

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
   */
  constructor(
    private studentService: StudentService,
    private router: Router,
    private location: Location,
    private activatedRoute: ActivatedRoute,
    private exerciseSheetService: ExerciseSheetsService
  ) {
    const nav = this.router.getCurrentNavigation();

    if (nav?.extras.state) {
      this._instance = nav.extras.state.instance;
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
    this.router.navigate(['task', taskUUID], { relativeTo: this.activatedRoute, state: this._instance });
  }

  /**
   * Asynchronously loads the student's tasks.
   */
  private async loadTasksAsync(): Promise<any> {
    const result = await this.studentService.getExerciseSheetTasks(this._instance!.instanceId, this._exerciseSheetUUID).toPromise();
    this.entries = result.body ?? [];
  }
}
