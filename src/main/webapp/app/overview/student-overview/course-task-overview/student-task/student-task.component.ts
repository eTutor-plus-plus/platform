import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { ICourseInstanceInformationDTO } from 'app/overview/shared/students/students.model';
import { lastValueFrom, Subscription } from 'rxjs';
import { TasksService } from 'app/overview/tasks/tasks.service';
import { ITaskModel, TaskAssignmentType, TaskDifficulty } from 'app/overview/tasks/task.model';
import { StudentService } from 'app/overview/shared/students/student-service';
import { TaskGroupManagementService } from '../../../tasks/tasks-overview/task-group-management/task-group-management.service';
import { ITaskGroupDTO } from '../../../tasks/tasks-overview/task-group-management/task-group-management.model';
import { FileUploadService } from '../../../shared/file-upload/file-upload.service';

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
  // apriori start data for apriori child element
  courseInstance: string;
  difficultyTask: string | undefined;
  // apriori end
  public isSaving = false;
  public isSubmitted = true;
  public exerciseSheetAlreadyClosed = false;
  public isUploadTask = false;
  public isCalcTask = false;
  public isDispatcherTask = false;
  public isPmTask = false;
  public uploadTaskFileId = -1;
  public uploadCalcSubmissionFileId = -1;
  public fDSubtype = '';

  public exercise_id = '';
  public task_type = '';
  public submission = '';
  public diagnoseLevel = -1;
  public achievedDispatcherPoints = 0;
  public maxPoints = '';
  public diagnoseLevelWeighting = '';
  public taskGroup: ITaskGroupDTO | undefined;
  public uploadFileId = -1;
  public writerIndividualInstructionFileId = -1;
  public calcIndividualInstructionFileId = -1;
  public calcIndividualSolutionFileId = -1;
  // public calcSolutionFileId = -1;
  public calcCorrectionFeedback: string | undefined;
  public matriculationNumber: string | undefined;
  public calcTaskPoints: number | undefined;

  public readonly _instance?: ICourseInstanceInformationDTO;
  public _taskNo = 0;
  public _exerciseSheetUUID = '';
  public _taskUUID = '';
  private _paramMapSubscription?: Subscription;
  private _taskModel?: ITaskModel;


  /**
   * Constructor.
   *
   * @param router the injected router
   * @param location the injected location service
   * @param activatedRoute the injected activated route
   * @param taskService the injected task service
   * @param studentService the injected student service
   * @param taskGroupService the injected task group service
   * @param fileService the injected file service
   */
  constructor(
    private router: Router,
    private location: Location,
    private activatedRoute: ActivatedRoute,
    private taskService: TasksService,
    private studentService: StudentService,
    private taskGroupService: TaskGroupManagementService,
    private fileService: FileUploadService
  ) {
    const nav = this.router.getCurrentNavigation();

    if (nav?.extras.state) {
      this._instance = nav.extras.state.instance;
      this.exerciseSheetAlreadyClosed = nav.extras.state.closed;
    } else {
      this.router.navigate(['/']);
    }

    // apriori start data for apriori child element
    this.courseInstance = this._instance!.instanceId;
    this.difficultyTask = this.taskModel?.taskDifficultyId;
    // apriori end
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
        const result = await lastValueFrom(this.taskService.getTaskAssignmentById(this._taskUUID, true));
        this._taskModel = result.body!;
        this.uploadFileId = this._taskModel.uploadFileId ?? -1;
        this.isUploadTask = this._taskModel.taskAssignmentTypeId === TaskAssignmentType.UploadTask.value;
        this.isCalcTask = this._taskModel.taskAssignmentTypeId === TaskAssignmentType.CalcTask.value;
        this.isDispatcherTask =
          this._taskModel.taskAssignmentTypeId === TaskAssignmentType.SQLTask.value ||
          this._taskModel.taskAssignmentTypeId === TaskAssignmentType.RATask.value ||
          this._taskModel.taskAssignmentTypeId === TaskAssignmentType.XQueryTask.value ||
          this._taskModel.taskAssignmentTypeId === TaskAssignmentType.BpmnTask.value ||
          this._taskModel.taskAssignmentTypeId === TaskAssignmentType.DatalogTask.value;

        this.isPmTask = this._taskModel.taskAssignmentTypeId === TaskAssignmentType.PmTask.value;

        if (this._taskModel.taskIdForDispatcher) {
          this.exercise_id = this._taskModel.taskIdForDispatcher;
        }
        if (this._taskModel.fDSubtype) {
          this.fDSubtype = this._taskModel.fDSubtype
        }
        if (this._taskModel.maxPoints) {
          this.maxPoints = this._taskModel.maxPoints;
        }


        // really this.isPmTask? +1
        if (this.isDispatcherTask || this.isPmTask) {
          this.task_type = this._taskModel.taskAssignmentTypeId;

          if (this._taskModel.diagnoseLevelWeighting) {
            this.diagnoseLevelWeighting = this._taskModel.diagnoseLevelWeighting;
          }

          this._taskModel.taskGroupId;
          this.submission = await lastValueFrom(
            this.studentService.getDispatcherSubmissionForIndividualTask(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
          );

          this.achievedDispatcherPoints = await lastValueFrom(
            this.studentService.getDispatcherPoints(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
          );

          this.diagnoseLevel = await lastValueFrom(
            this.studentService.getDiagnoseLevel(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
          );
        }

        if (this.isUploadTask || this.isDispatcherTask) {
          this.uploadTaskFileId = await lastValueFrom(
            this.studentService.getFileAttachmentId(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
          );
        }
        if (this.isCalcTask) {
          this.uploadCalcSubmissionFileId = await lastValueFrom(
            this.studentService.getFileAttachmentId(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
          );

          this.writerIndividualInstructionFileId = await lastValueFrom(
            this.studentService.getFileAttachmentIdOfIndividualWriterInstruction(
              this._instance!.instanceId,
              this._exerciseSheetUUID,
              this._taskNo
            )
          );

          this.calcIndividualInstructionFileId = await lastValueFrom(
            this.studentService.getFileAttachmentIdOfIndividualCalcInstruction(
              this._instance!.instanceId,
              this._exerciseSheetUUID,
              this._taskNo
            )
          );

          this.calcIndividualSolutionFileId = await lastValueFrom(
            this.studentService.getFileAttachmentIdOfIndividualCalcSolution(
              this._instance!.instanceId,
              this._exerciseSheetUUID,
              this._taskNo
            )
          );

          // this.calcSolutionFileId = await this.taskService.getFileIdOfCalcSolution(this._taskUUID, true).toPromise();

          this.studentService.getMatriculationNumberOfLoggedInStudent().subscribe(data => {
            this.matriculationNumber = data;
          });
          this.updateCalcTaskPoints();
        }
        const taskGroupId = this._taskModel.taskGroupId;
        if (taskGroupId) {
          this.taskGroupService
            .getTaskGroup(taskGroupId.substr(taskGroupId.indexOf('#') + 1))
            .subscribe(taskGroup => (this.taskGroup = taskGroup));
        }
      })();

      (async () => {
        this.isSubmitted = await lastValueFrom(
          this.studentService.isTaskSubmitted(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo)
        );
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
   * Asynchronously adds the calc file.
   *
   * @param fileId
   */
  public async handleCalcFileAddedAsync(fileId: number): Promise<void> {
    await this.studentService
      .setUploadTaskAttachment(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, fileId)
      .toPromise();
    this.fileService.getFileMetaData(fileId).subscribe(data => {
      if (data.contentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
        this.uploadCalcSubmissionFileId = fileId;
      } else {
        this.uploadCalcSubmissionFileId = -2;
      }
    });
  }

  /**
   * Asynchronously removes the calc file.
   *
   * @param fileId
   */
  public async handleCalcFileRemovedAsync(fileId: number): Promise<void> {
    await this.studentService
      .removeUploadTaskAttachment(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, fileId)
      .toPromise();
    this.uploadCalcSubmissionFileId = -1;
  }

  /**
   * Asynchronously sets a modified calc file.
   *
   * @param oldFileId the file's old id
   * @param newFileId the file's new id
   */
  public async handleCalcFileMovedAsync(oldFileId: number, newFileId: number): Promise<void> {
    await this.studentService
      .setUploadTaskAttachment(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, newFileId)
      .toPromise();
    this.fileService.getFileMetaData(newFileId).subscribe(data => {
      if (data.contentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
        this.uploadCalcSubmissionFileId = newFileId;
      } else {
        this.uploadCalcSubmissionFileId = -2;
      }
    });
  }

  /**
   * corrects the submitted calc tasks and saves the feedback on .calcCorrectionFeedback
   */
  public correctCalcTask(): void {
    if (
      this.uploadCalcSubmissionFileId !== -1 &&
      this.uploadCalcSubmissionFileId !== -2 &&
      this.writerIndividualInstructionFileId !== -1 &&
      this.calcIndividualSolutionFileId !== -1 &&
      this.calcIndividualInstructionFileId !== -1 &&
      this.matriculationNumber !== undefined &&
      this._instance !== undefined
    ) {
      this.studentService
        .getCorrectionOfCalcTaskDatabase(
          this.matriculationNumber,
          this._instance.instanceId,
          this._exerciseSheetUUID,
          this._taskNo,
          this.writerIndividualInstructionFileId,
          this.calcIndividualSolutionFileId,
          this.uploadCalcSubmissionFileId
        )
        .subscribe();
      this.studentService
        .getCorrectionOfCalcTask(this.writerIndividualInstructionFileId, this.calcIndividualSolutionFileId, this.uploadCalcSubmissionFileId)
        .subscribe(data => {
          this.calcCorrectionFeedback = data;
        });
    }
  }

  /**
   * submits the submitted calc task and sets the dispatcher points
   */
  public submitCalcTask(): void {
    if (
      this.uploadCalcSubmissionFileId !== -1 &&
      this.uploadCalcSubmissionFileId !== -2 &&
      this.writerIndividualInstructionFileId !== -1 &&
      this.calcIndividualSolutionFileId !== -1 &&
      this.calcIndividualInstructionFileId !== -1 &&
      this.matriculationNumber !== undefined &&
      this._instance !== undefined
    ) {
      this.studentService
        .handleCalcTaskSubmission(
          this.matriculationNumber,
          this._instance.instanceId,
          this._exerciseSheetUUID,
          this._taskNo,
          this.writerIndividualInstructionFileId,
          this.calcIndividualSolutionFileId,
          this.uploadCalcSubmissionFileId
        )
        .subscribe(() => {
          this.updateCalcTaskPoints();
        });
    }
  }

  /**
   * Redirects the UUID from the dispatcher to the server
   * @param $event the UUID
   */
  public handleDispatcherUUID($event: string): void {
    this.studentService
      .processDispatcherSubmissionForIndividualTask(this._instance!.instanceId, this._exerciseSheetUUID, this._taskNo, $event)
      .subscribe(n => (this.achievedDispatcherPoints = n));
  }

  /**
   * updates the achieved calc task points
   */
  public updateCalcTaskPoints(): void {
    if (this._instance !== undefined) {
      this.studentService.getDispatcherPoints(this._instance.instanceId, this._exerciseSheetUUID, this._taskNo).subscribe(data => {
        this.calcTaskPoints = data;
      });
    }
  }

  protected readonly TaskAssignmentType = TaskAssignmentType;
}
