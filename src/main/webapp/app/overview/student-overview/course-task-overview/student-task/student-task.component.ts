import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { ICourseInstanceInformationDTO } from 'app/overview/shared/students/students.model';
import { Subscription } from 'rxjs';
import { TasksService } from 'app/overview/tasks/tasks.service';
import { ITaskModel, TaskAssignmentType, TaskDifficulty } from 'app/overview/tasks/task.model';
import { StudentService } from 'app/overview/shared/students/student-service';

// noinspection JSIgnoredPromiseFromCall
/**
 * Component for displaying students' tasks.
 */
@Component({
  selector: 'jhi-student-task',
  templateUrl: './student-task.component.html',
  styleUrls: ['./student-task.component.scss'],
})
export class StudentTaskComponent implements OnInit, OnDestroy {
  public isSaving = false;
  public isSubmitted = true;
  public exerciseSheetAlreadyClosed = false;
  public isUploadTask = false;
  public isDispatcherTask = true;
  public uploadTaskFileId = -1;

  public exercise_id = '';
  public task_type = '';
  public submission = '';
  public diagnoseLevel = 0;
  public dispatcherPoints = 0;
  public maxPoints = 10;

  private readonly _instance?: ICourseInstanceInformationDTO;
  private _paramMapSubscription?: Subscription;
  private _exerciseSheetUUID = '';
  private _taskUUID = '';
  private _taskNo = 0;
  private _taskModel?: ITaskModel;

  /**
   * Constructor.
   *
   * @param router the injected router
   * @param location the injected location service
   * @param activatedRoute the injected activated route
   * @param taskService the injected task service
   * @param studentService the injected student service
   */
  constructor(
    private router: Router,
    private location: Location,
    private activatedRoute: ActivatedRoute,
    private taskService: TasksService,
    private studentService: StudentService
  ) {
    const nav = this.router.getCurrentNavigation();

    if (nav?.extras.state) {
      this._instance = nav.extras.state.instance;
      this.exerciseSheetAlreadyClosed = nav.extras.state.closed;
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
      this._taskUUID = paramMap.get('taskUUID')!;
      this._taskNo = Number(paramMap.get('taskNo')!);

      (async () => {
        const result = await this.taskService.getTaskAssignmentById(this._taskUUID, true).toPromise();
        this._taskModel = result.body!;

        this.isUploadTask = this._taskModel.taskAssignmentTypeId === TaskAssignmentType.UploadTask.value;
        this.isDispatcherTask = this._taskModel.taskAssignmentTypeId === TaskAssignmentType.SQLTask.value;

        if (this.isDispatcherTask) {
          this.task_type = this._taskModel.taskAssignmentTypeId;

          if (this._taskModel.taskIdForDispatcher) {
            this.exercise_id = this._taskModel.taskIdForDispatcher;
          }

          if (this._taskModel.maxPoints) {
            this.maxPoints = this._taskModel.maxPoints;
          }

          this.submission = await this.studentService
            .getSubmissionForAssignment(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
            .toPromise();

          this.dispatcherPoints = await this.studentService
            .getDispatcherPoints(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
            .toPromise();

          this.diagnoseLevel = await this.studentService
            .getDiagnoseLevel(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
            .toPromise();
        }

        if (this.isUploadTask) {
          this.uploadTaskFileId = await this.studentService
            .getFileAttachmentId(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
            .toPromise();
        }
      })();

      (async () => {
        this.isSubmitted = await this.studentService
          .isTaskSubmitted(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
          .toPromise();
      })();
    });
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  public ngOnDestroy(): void {
    this._paramMapSubscription?.unsubscribe();
  }

  /**
   * Returns the task model.
   */
  public get taskModel(): ITaskModel | undefined {
    return this._taskModel;
  }

  /**
   * Navigates back.
   */
  public navigateBack(): void {
    this.location.back();
  }

  /**
   * Returns the i18n string of a given difficulty url.
   *
   * @param url the url
   */
  public getDifficultyI18nString(url?: string): string {
    if (!url) {
      return '';
    }
    return TaskDifficulty.fromString(url)!.text;
  }

  /**
   * Asynchronously marks the task as submitted.
   */
  public async markTaskAsSubmittedAsync(): Promise<void> {
    this.isSaving = true;
    try {
      await this.studentService.markTaskAsSubmitted(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo).toPromise();
    } catch (e) {
      this.isSaving = false;
      throw e;
    }
    this.isSubmitted = true;
    this.isSaving = false;
  }

  /**
   * Asynchronously adds the file.
   *
   * @param fileId the file to add
   */
  public async handleFileAddedAsync(fileId: number): Promise<void> {
    await this.studentService
      .setUploadTaskAttachment(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, fileId)
      .toPromise();
    this.uploadTaskFileId = fileId;
  }

  /**
   * Asynchronously removes the file.
   *
   * @param fileId the file to remove
   */
  public async handleFileRemovedAsync(fileId: number): Promise<void> {
    await this.studentService
      .removeUploadTaskAttachment(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, fileId)
      .toPromise();
    this.uploadTaskFileId = -1;
  }

  /**
   * Asynchronously sets a modified file.
   *
   * @param oldFileId the file's old id
   * @param newFileId the file's new id
   */
  public async handleFileMovedAsync(oldFileId: number, newFileId: number): Promise<void> {
    await this.studentService
      .setUploadTaskAttachment(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, newFileId)
      .toPromise();
    this.uploadTaskFileId = newFileId;
  }

  /**
   * Asynchronously marks the task as submitted and sets the points for an assignment as graded by the dispatcher
   */
  public async handleSolutionCorrectAsync($event: number): Promise<void> {
    await this.markTaskAsSubmittedAsync();
    await this.studentService.setDispatcherPoints(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, $event).toPromise();
  }
  /**
   * Asynchronously adds a submission
   * @param submission the submission
   */
  public async handleSubmissionAddedAsync(submission: string): Promise<void> {
    await this.studentService
      .setSubmissionForAssignment(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, submission)
      .toPromise();
  }

  /**
   * Asynchronously sets the highest chosen diagnose level
   * @param $event
   */
  public async handleDiagnoseLevelIncreasedAsync($event: number): Promise<void> {
    await this.studentService.setDiagnoseLevel(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, $event).toPromise();
  }
}
