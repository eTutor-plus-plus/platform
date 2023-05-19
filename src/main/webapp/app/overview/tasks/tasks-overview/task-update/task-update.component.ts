import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TasksService } from '../../tasks.service';
import { INewTaskModel, ITaskModel, TaskAssignmentType, TaskDifficulty } from '../../task.model';
import { CustomValidators } from 'app/shared/validators/custom-validators';
import { URL_OR_EMPTY_PATTERN } from 'app/config/input.constants';
import { EventManager } from 'app/core/util/event-manager.service';
import { ITaskGroupDisplayDTO } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.model';
import { TaskGroupManagementService } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.service';
import { SqlExerciseService } from 'app/overview/dispatcher/services/sql-exercise.service';
import { DispatcherAssignmentModalComponent } from '../../../dispatcher/dispatcher-assignment-modal/dispatcher-assignment-modal.component';
import { FileUploadService } from '../../../shared/file-upload/file-upload.service';

/**
 * Component for creating / updating tasks.
 */
@Component({
  selector: 'jhi-task-update',
  templateUrl: './task-update.component.html',
})
export class TaskUpdateComponent implements OnInit {
  public isSaving = false;
  public readonly difficulties = TaskDifficulty.Values;
  public readonly taskTypes = TaskAssignmentType.Values;
  public editorOptions = { theme: 'vs-light', language: 'pgsql' };
  public editorOptionsXQ = { theme: 'xquery-light', language: 'xquery' };
  public editorOptionsReadOnly = { theme: 'vs-light', language: 'pgsql', readOnly: true };
  public editorOptionsXMLReadOnly = { theme: 'vs-light', language: 'xml', readOnly: true };
  public editorOptionsDLG = { theme: 'datalog-light', language: 'datalog' };
  public editorOptionsDLGReadOnly = { theme: 'datalog-light', language: 'datalog', readOnly: true };
  public isSQLTask = false;
  public isRATask = false;
  public isXQueryTask = false;
  public isDLQTask = false;
  public isBpmnTask = false;
  public isPmTask = false; // boolean flag
  public isCalcTask = false;
  public isOwlTask = false;
  public taskGroups: ITaskGroupDisplayDTO[] = [];
  public uploadFileId = -1;
  public writerInstructionFileId = -1;
  public calcSolutionFileId = -1;
  public calcInstructionFileId = -1;
  public startTime = null;
  public endTime = null;

  public readonly updateForm = this.fb.group({
    header: ['', [CustomValidators.required]],
    creator: ['', [CustomValidators.required]],
    organisationUnit: ['', [CustomValidators.required]],
    privateTask: [false],
    taskDifficulty: [this.difficulties[0], [Validators.required]],
    taskAssignmentType: [this.taskTypes[0], [Validators.required]],
    taskIdForDispatcher: [''],
    sqlCreateStatements: [''],
    sqlInsertStatementsSubmission: [''],
    sqlInsertStatementsDiagnose: [''],
    xQueryDiagnoseXML: [''],
    xQuerySubmissionXML: [''],
    xQueryFileURL: [''],
    sqlSolution: [''],
    xQuerySolution: [''],
    xQueryXPathSorting: [''],
    datalogFacts: [''],
    datalogSolution: [''],
    datalogQuery: [''],
    datalogUncheckedTerms: [''],
    maxPoints: [''],
    owlStatement: [''],
    startTime: [''],
    endTime: [''],
    diagnoseLevelWeighting: [''],
    processingTime: [''],
    url: ['', [Validators.pattern(URL_OR_EMPTY_PATTERN)]],
    instruction: [''],
    taskGroup: ['', []],
    bpmnTestConfig: [''],
    // PM related variables:
    maxActivity: [''],
    minActivity: [''],
    maxLogSize: [''],
    minLogSize: [''],
    configNum: [''],
  });

  private _taskModel?: ITaskModel;

  /**
   * Constructor.
   *
   * @param fb the injected form builder service
   * @param activeModal the injected active modal service
   * @param modalService the injected modal service
   * @param tasksService the injected tasks service
   * @param eventManager the injected event manager service
   * @param taskGroupService the task group service
   * @param sqlExerciseService the injected SQL exercise service
   * @param fileService the injected File service
   */
  constructor(
    private fb: FormBuilder,
    private activeModal: NgbActiveModal,
    private modalService: NgbModal,
    private tasksService: TasksService,
    private eventManager: EventManager,
    private sqlExerciseService: SqlExerciseService,
    private taskGroupService: TaskGroupManagementService,
    private fileService: FileUploadService
  ) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.taskGroupService
      .getPagedTaskGroups({
        page: 0,
        size: 1000,
        sort: [],
      })
      .subscribe(response => {
        this.taskGroups = response.body ?? [];
      });
  }

  /**
   * Saves the task.
   */
  public save(): void {
    this.isSaving = true;

    const taskDifficultyId = (this.updateForm.get(['taskDifficulty'])!.value as TaskDifficulty).value;
    const taskAssignmentTypeId = (this.updateForm.get(['taskAssignmentType'])!.value as TaskAssignmentType).value;

    const newTask: INewTaskModel = {
      header: this.updateForm.get(['header'])!.value.trim(),
      creator: this.updateForm.get(['creator'])!.value.trim(),
      organisationUnit: this.updateForm.get(['organisationUnit'])!.value.trim(),
      taskDifficultyId,
      taskAssignmentTypeId,
      privateTask: this.updateForm.get('privateTask')!.value,
      learningGoalIds: [],
      taskGroupId: this.updateForm.get(['taskGroup'])!.value,
      uploadFileId: this.uploadFileId,
      writerInstructionFileId: this.writerInstructionFileId,
      calcSolutionFileId: this.calcSolutionFileId,
      calcInstructionFileId: this.calcInstructionFileId,
    };

    const urlStr: string = this.updateForm.get('url')!.value;
    if (urlStr) {
      newTask.url = new URL(urlStr);
    }

    const owlStatement: string = this.updateForm.get('owlStatement')!.value;
    if (owlStatement) {
      newTask.owlStatement = owlStatement;
    }

    const instructionStr: string = this.updateForm.get('instruction')!.value;
    if (instructionStr.trim()) {
      newTask.instruction = instructionStr.trim();
    }

    const taskIdForDispatcher: string = this.updateForm.get('taskIdForDispatcher')!.value;
    if (taskIdForDispatcher) {
      newTask.taskIdForDispatcher = taskIdForDispatcher;
    }

    const sqlSolution: string = this.updateForm.get('sqlSolution')!.value;
    if (sqlSolution) {
      newTask.sqlSolution = sqlSolution;
    }

    const xQuerySolution: string = this.updateForm.get('xQuerySolution')!.value;
    if (xQuerySolution) {
      newTask.xQuerySolution = xQuerySolution;
    }

    const xQueryXPathSorting: string = this.updateForm.get('xQueryXPathSorting')!.value;
    if (xQueryXPathSorting) {
      newTask.xQueryXPathSorting = xQueryXPathSorting;
    }

    const datalogSolution: string = this.updateForm.get('datalogSolution')!.value;
    if (datalogSolution) {
      newTask.datalogSolution = datalogSolution;
    }

    const datalogQuery: string = this.updateForm.get('datalogQuery')!.value;
    if (datalogQuery) {
      newTask.datalogQuery = datalogQuery;
    }

    const datalogUncheckedTerms: string = this.updateForm.get('datalogUncheckedTerms')!.value;
    if (datalogUncheckedTerms) {
      newTask.datalogUncheckedTerms = datalogUncheckedTerms;
    }

    const maxPoints: string = this.updateForm.get('maxPoints')!.value;
    if (maxPoints) {
      newTask.maxPoints = maxPoints;
    }

    const startTime: string = this.updateForm.get('startTime')!.value;
    if (startTime) {
      newTask.startTime = startTime;
    }

    const endTime: string = this.updateForm.get('endTime')!.value;
    if (endTime) {
      newTask.endTime = endTime;
    }

    const diagnoseLevelWeighting: string = this.updateForm.get('diagnoseLevelWeighting')!.value;
    if (diagnoseLevelWeighting) {
      newTask.diagnoseLevelWeighting = diagnoseLevelWeighting;
    }
    const processingTime: string = this.updateForm.get('processingTime')!.value;
    if (processingTime.trim()) {
      newTask.processingTime = processingTime.trim();
    }
    const bpmnTestConfig: string = this.updateForm.get('bpmnTestConfig')!.value;
    if (bpmnTestConfig) {
      newTask.bpmnTestConfig = bpmnTestConfig;
    }

    // variables related to PM Task
    const maxActivity: number = this.updateForm.get('maxActivity')!.value;
    if (maxActivity) {
      newTask.maxActivity = maxActivity;
    }
    const minActivity: number = this.updateForm.get('minActivity')!.value;
    if (minActivity) {
      newTask.minActivity = minActivity;
    }
    const maxLogSize: number = this.updateForm.get('maxLogSize')!.value;
    if (maxLogSize) {
      newTask.maxLogSize = maxLogSize;
    }
    const minLogSize: number = this.updateForm.get('minLogSize')!.value;
    if (minLogSize) {
      newTask.minLogSize = minLogSize;
    }
    const configNum: string = this.updateForm.get('configNum')!.value;
    if (configNum) {
      newTask.configNum = configNum;
    }

    if (this.isNew) {
      this.tasksService.saveNewTask(newTask).subscribe(
        () => {
          this.isSaving = false;
          this.eventManager.broadcast('taskModification');
          this.close();
        },
        () => (this.isSaving = false)
      );
    } else {
      const editedTask: ITaskModel = {
        header: newTask.header,
        creator: newTask.creator,
        organisationUnit: newTask.organisationUnit,
        taskDifficultyId: newTask.taskDifficultyId,
        taskIdForDispatcher: newTask.taskIdForDispatcher,
        sqlSolution: newTask.sqlSolution,
        xQuerySolution: newTask.xQuerySolution,
        xQueryXPathSorting: newTask.xQueryXPathSorting,
        datalogSolution: newTask.datalogSolution,
        datalogQuery: newTask.datalogQuery,
        datalogUncheckedTerms: newTask.datalogUncheckedTerms,
        maxPoints: newTask.maxPoints,
        diagnoseLevelWeighting: newTask.diagnoseLevelWeighting,
        processingTime: newTask.processingTime,
        bpmnTestConfig: newTask.bpmnTestConfig,
        maxActivity: newTask.maxActivity,
        minActivity: newTask.minActivity,
        maxLogSize: newTask.maxLogSize,
        minLogSize: newTask.minLogSize,
        configNum: newTask.configNum,
        url: newTask.url,
        instruction: newTask.instruction,
        privateTask: newTask.privateTask,
        taskAssignmentTypeId: newTask.taskAssignmentTypeId,
        taskGroupId: newTask.taskGroupId,
        creationDate: this.taskModel!.creationDate,
        id: this.taskModel!.id,
        internalCreator: this.taskModel!.internalCreator,
        learningGoalIds: this.taskModel!.learningGoalIds,
        uploadFileId: this.uploadFileId,
        writerInstructionFileId: this.writerInstructionFileId,
        calcSolutionFileId: this.calcSolutionFileId,
        calcInstructionFileId: this.calcInstructionFileId,
        startTime: newTask.startTime,
        endTime: newTask.endTime,
        owlStatement: newTask.owlStatement,
      };

      this.tasksService.saveEditedTask(editedTask).subscribe(
        () => {
          this.isSaving = false;
          this.eventManager.broadcast('taskModification');
          this.close();
        },
        () => (this.isSaving = false)
      );
    }
  }

  /**
   * Closes the modal dialog.
   */
  public close(): void {
    this.activeModal.close();
  }

  /**
   * Sets the task model.
   *
   * @param value the task model to set
   */
  public set taskModel(value: ITaskModel | undefined) {
    this._taskModel = value;

    if (value) {
      const taskDifficulty = this.difficulties.find(x => x.value === value.taskDifficultyId)!;
      const taskAssignmentType = this.taskTypes.find(x => x.value === value.taskAssignmentTypeId);
      const taskIdForDispatcher = value.taskIdForDispatcher ?? '';
      const bpmnTestConfig = value.bpmnTestConfig ?? '';
      const sqlSolution = value.sqlSolution;
      const xQuerySolution = value.xQuerySolution;
      const xQueryXPathSorting = value.xQueryXPathSorting;
      const datalogSolution = value.datalogSolution;
      const datalogQuery = value.datalogQuery;
      const datalogUncheckedTerms = value.datalogUncheckedTerms;
      const maxPoints = value.maxPoints ?? '';
      const startTime = value.startTime ?? '';
      const endTime = value.endTime ?? '';
      const diagnoseLevelWeighting = value.diagnoseLevelWeighting ?? '';
      const processingTime = value.processingTime ?? '';
      const url = value.url ? value.url.toString() : '';
      const instruction = value.instruction ?? '';
      const taskGroupId = value.taskGroupId ?? '';
      const taskAssignmentTypeId = value.taskAssignmentTypeId;
      // PM related variables
      const maxActivity = value.maxActivity;
      const minActivity = value.minActivity;
      const maxLogSize = value.maxLogSize;
      const minLogSize = value.minLogSize;
      const configNum = value.configNum;
      const owlStatement = value.owlStatement;

      if (taskIdForDispatcher) {
        this.updateForm.get('taskIdForDispatcher')!.disable();
      }
      this.updateForm.get('xQueryFileURL')!.disable();

      this.patchDispatcherValues(taskAssignmentTypeId, taskGroupId);

      this.updateForm.patchValue({
        header: value.header,
        creator: value.creator,
        organisationUnit: value.organisationUnit,
        privateTask: value.privateTask,
        taskDifficulty,
        taskAssignmentType,
        taskIdForDispatcher,
        sqlSolution,
        xQuerySolution,
        xQueryXPathSorting,
        datalogSolution,
        datalogQuery,
        datalogUncheckedTerms,
        maxPoints,
        startTime,
        endTime,
        diagnoseLevelWeighting,
        processingTime,
        url,
        instruction,
        taskGroup: value.taskGroupId ?? '',
        bpmnTestConfig,
        // PM related variables
        maxActivity,
        minActivity,
        maxLogSize,
        minLogSize,
        configNum,
        owlStatement,
      });
      this.taskTypeChanged();
      this.uploadFileId = value.uploadFileId ?? -1;
      this.writerInstructionFileId = value.writerInstructionFileId ?? -1;
      this.calcSolutionFileId = value.calcSolutionFileId ?? -1;
      this.calcInstructionFileId = value.calcInstructionFileId ?? -1;
    }
  }

  /**
   * Returns the task model
   */
  public get taskModel(): ITaskModel | undefined {
    return this._taskModel;
  }

  /**
   * Checks if the taskType requires additional form fields in the course of "set TaskModel".
   * Fetches additional values from the taskGroup and patches it in the update form.
   * @param taskAssignmentTypeId
   * @param taskGroupId
   * @private
   */
  public patchDispatcherValues(taskAssignmentTypeId: string, taskGroupId: string): void {
    if (this.isSqlOrRaTask(taskAssignmentTypeId)) {
      if (taskAssignmentTypeId === TaskAssignmentType.SQLTask.value) {
        this.isSQLTask = true;
      } else {
        this.isRATask = true;
      }
      this.patchSqlTaskGroupValues(taskGroupId);
    } else if (taskAssignmentTypeId === TaskAssignmentType.XQueryTask.value) {
      this.isXQueryTask = true;
      this.patchXQueryTaskGroupValues(taskGroupId);
    } else if (taskAssignmentTypeId === TaskAssignmentType.DatalogTask.value) {
      this.isDLQTask = true;
      this.patchDatalogTaskGroupValues(taskGroupId);
    } else if (taskAssignmentTypeId === TaskAssignmentType.CalcTask.value) {
      this.isCalcTask = true;
    }
  }
  /**
   * Returns whether this modal window is in new mode.
   * {@code true} = new mode, {@code false} = edit mode
   */
  public get isNew(): boolean {
    return this._taskModel === undefined;
  }

  /**
   * Reacts to a change of the taskType in the update form
   */
  public taskTypeChanged(): void {
    const taskAssignmentTypeId = (this.updateForm.get(['taskAssignmentType'])!.value as TaskAssignmentType).value;

    this.setAllTaskTypeFlagsToFalse();
    this.clearAllTaskTypeDependentValidators();

    if (taskAssignmentTypeId === TaskAssignmentType.SQLTask.value) {
      this.isSQLTask = true;
    } else if (taskAssignmentTypeId === TaskAssignmentType.XQueryTask.value) {
      this.isXQueryTask = true;
    } else if (taskAssignmentTypeId === TaskAssignmentType.DatalogTask.value) {
      this.isDLQTask = true;
    } else if (taskAssignmentTypeId === TaskAssignmentType.RATask.value) {
      this.isRATask = true;
    } else if (taskAssignmentTypeId === TaskAssignmentType.BpmnTask.value) {
      this.isBpmnTask = true;
      this.setMaxPointsRequired();
    } else if (taskAssignmentTypeId === TaskAssignmentType.PmTask.value) {
      this.isPmTask = true;
    } else if (taskAssignmentTypeId === TaskAssignmentType.CalcTask.value) {
      this.isCalcTask = true;
      this.setMaxPointsRequired();
    } else if (taskAssignmentTypeId === TaskAssignmentType.OWLTask.value) {
      this.isOwlTask = true;
      this.updateForm.get('owlStatement')!.setValidators(Validators.required);
      this.updateForm.get('owlStatement')!.updateValueAndValidity();
      this.updateForm.updateValueAndValidity();
    }

    if (this.isDkeDispatcherTask(taskAssignmentTypeId)) {
      if (!this.updateForm.get('taskIdForDispatcher')!.value) {
        this.setTaskGroupRequired();
      }
      this.setMaxPointsRequired();
      this.setDiagnoseLevelWeightingRequired();
    }
    this.updateForm.updateValueAndValidity();
  }
  /**
   * Reacts to a change of the taskGroup by patching the relevant data from the group in the update form
   */
  public taskGroupChanged(): void {
    const taskGroupId = this.updateForm.get(['taskGroup'])!.value as string | undefined;
    const taskT = (this.updateForm.get(['taskAssignmentType'])!.value as TaskAssignmentType).value;
    if (this.isSqlOrRaTask(taskT)) {
      this.patchSqlTaskGroupValues(taskGroupId);
    } else if (taskT === TaskAssignmentType.XQueryTask.value) {
      this.patchXQueryTaskGroupValues(taskGroupId);
    } else if (taskT === TaskAssignmentType.DatalogTask.value) {
      this.patchDatalogTaskGroupValues(taskGroupId);
    }
  }

  /**
   * Reacts to the input of a task-id for the dispatcher
   */
  public taskIdForDispatcherEntered(): void {
    const taskAssignmentTypeId = (this.updateForm.get(['taskAssignmentType'])!.value as TaskAssignmentType).value;

    if (
      taskAssignmentTypeId === TaskAssignmentType.SQLTask.value ||
      taskAssignmentTypeId === TaskAssignmentType.XQueryTask.value ||
      taskAssignmentTypeId === TaskAssignmentType.DatalogTask.value ||
      taskAssignmentTypeId === TaskAssignmentType.RATask.value ||
      taskAssignmentTypeId === TaskAssignmentType.BpmnTask.value
    ) {
      if (this.updateForm.get('taskIdForDispatcher')!.value) {
        this.updateForm.get('taskGroup')?.clearValidators();
        this.updateForm.get('taskGroup')?.updateValueAndValidity();
        this.updateForm.updateValueAndValidity();
      } else {
        this.updateForm.get('taskGroup')!.setValidators(Validators.required);
        this.updateForm.get('taskGroup')!.updateValueAndValidity();
        this.updateForm.updateValueAndValidity();
      }
    } else {
      // === PmTask
    }
  }

  /**
   * Opens a lecturer-run-submission modal window to test a solution
   */
  public openSolutionRunnerWindow(asSql = false): void {
    const modalRef = this.modalService.open(DispatcherAssignmentModalComponent, { backdrop: 'static', size: 'xl' });
    let subm = '';
    const taskT = asSql
      ? TaskAssignmentType.SQLTask.value
      : (this.updateForm.get(['taskAssignmentType'])!.value as TaskAssignmentType).value;
    if (taskT === TaskAssignmentType.SQLTask.value) {
      subm = this.updateForm.get(['sqlSolution'])?.value ?? '';
    } else if (taskT === TaskAssignmentType.XQueryTask.value) {
      subm = this.updateForm.get(['xQuerySolution'])?.value ?? '';
    } else if (taskT === TaskAssignmentType.DatalogTask.value) {
      subm = this.updateForm.get(['datalogSolution'])?.value ?? '';
    }
    const id = this.updateForm.get(['taskIdForDispatcher'])!.value;
    (modalRef.componentInstance as DispatcherAssignmentModalComponent).submissionEntry = {
      hasBeenSolved: false,
      isSubmitted: false,
      instant: '',
      submission: subm,
      dispatcherId: id,
      taskType: taskT,
    };
    (modalRef.componentInstance as DispatcherAssignmentModalComponent).showHeader = false;
    (modalRef.componentInstance as DispatcherAssignmentModalComponent).showSubmitButton = true;
  }

  /**
   * Sets the file id.
   *
   * @param fileId the file to add
   */
  public handleFileAdded(fileId: number): void {
    this.uploadFileId = fileId;
  }

  /**
   * Removes the file.
   *
   * @param fileId the file to remove
   */
  public handleFileRemoved(fileId: number): void {
    this.uploadFileId = -1;
  }

  /**
   * Sets a modified file.
   *
   * @param oldFileId the file's old id
   * @param newFileId the file's new id
   */
  public handleFileMoved(oldFileId: number, newFileId: number): void {
    this.uploadFileId = newFileId;
  }

  /*
  Opaque ID transformation. Thomas Hollin
   */
  public createMoodleId(id: string | undefined): string {
    let moodleId = '';
    if (id) {
      const tempId = id.substring(id.indexOf('#') + 1);
      moodleId = tempId.replace(/-/gi, '_');
      moodleId = 'moodle'.concat(moodleId);
    }
    return moodleId;
  }

  public copyInputMessage(inputElement: { select: () => void; setSelectionRange: (arg0: number, arg1: number) => void }): void {
    inputElement.select();
    document.execCommand('copy');
    inputElement.setSelectionRange(0, 0);
  }

  /**
   * Sets the writer instruction id.
   *
   * @param fileId the file to add
   */
  public handleWriterInstructionFileAdded(fileId: number): void {
    this.fileService.getFileMetaData(fileId).subscribe(data => {
      if (data.contentType === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') {
        this.writerInstructionFileId = fileId;
      } else {
        this.writerInstructionFileId = -2;
      }
    });
  }

  /**
   * Removes the writer instruction file.
   *
   * @param fileId the file to remove
   */
  public handleWriterInstructionFileRemoved(fileId: number): void {
    this.writerInstructionFileId = -1;
  }

  /**
   * Sets a modified  writer instruction file.
   *
   * @param oldFileId the file's old id
   * @param newFileId the file's new id
   */
  public handleWriterInstructionFileMoved(oldFileId: number, newFileId: number): void {
    this.fileService.getFileMetaData(newFileId).subscribe(data => {
      if (data.contentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
        this.writerInstructionFileId = newFileId;
      } else {
        this.writerInstructionFileId = -2;
      }
    });
  }

  /**
   * Sets the calc solution file id.
   *
   * @param fileId the file to add
   */
  public handleCalcSolutionFileAdded(fileId: number): void {
    this.fileService.getFileMetaData(fileId).subscribe(data => {
      if (data.contentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
        this.calcSolutionFileId = fileId;
      } else {
        this.calcSolutionFileId = -2;
      }
    });
  }

  /**
   * Removes the calc solution file.
   *
   * @param fileId the file to remove
   */
  public handleCalcSolutionFileRemoved(fileId: number): void {
    this.calcSolutionFileId = -1;
  }

  /**
   * Sets a modified solution calc file.
   *
   * @param oldFileId the file's old id
   * @param newFileId the file's new id
   */
  public handleCalcSolutionFileMoved(oldFileId: number, newFileId: number): void {
    this.fileService.getFileMetaData(newFileId).subscribe(data => {
      if (data.contentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
        this.calcSolutionFileId = newFileId;
      } else {
        this.calcSolutionFileId = -2;
      }
    });
  }

  /**
   * Sets the calc instruction id.
   *
   * @param fileId the file to add
   */
  public handleCalcInstructionFileAdded(fileId: number): void {
    this.fileService.getFileMetaData(fileId).subscribe(data => {
      if (data.contentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
        this.calcInstructionFileId = fileId;
      } else {
        this.calcInstructionFileId = -2;
      }
    });
  }

  /**
   * Removes the calc instruction file.
   *
   * @param fileId the file to remove
   */
  public handleCalcInstructionFileRemoved(fileId: number): void {
    this.calcInstructionFileId = -1;
  }

  /**
   * Sets a modified  calc instruction file.
   *
   * @param oldFileId the file's old id
   * @param newFileId the file's new id
   */
  public handleCalcInstructionFileMoved(oldFileId: number, newFileId: number): void {
    this.fileService.getFileMetaData(newFileId).subscribe(data => {
      if (data.contentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
        this.calcInstructionFileId = newFileId;
      } else {
        this.calcInstructionFileId = -2;
      }
    });
  }

  /**
   * Patches the values from an SQL-Task group in the update form
   * @param taskGroupId the task-group-id
   */
  private patchSqlTaskGroupValues(taskGroupId: string | undefined): void {
    if (taskGroupId) {
      const taskGroupName = taskGroupId.substring(taskGroupId.indexOf('#') + 1);
      this.taskGroupService.getTaskGroup(taskGroupName).subscribe(taskGroupDTO => {
        this.updateForm.patchValue({
          sqlCreateStatements: taskGroupDTO.sqlCreateStatements,
          sqlInsertStatementsDiagnose: taskGroupDTO.sqlInsertStatementsDiagnose,
          sqlInsertStatementsSubmission: taskGroupDTO.sqlInsertStatementsSubmission,
        });
      });
    }
  }

  /**
   * Patches the values from an XQ-Task group in the update form
   * @param taskGroupId the task-group-id
   * @private
   */
  private patchXQueryTaskGroupValues(taskGroupId: string | undefined): void {
    this.updateForm.get('xQueryFileURL')?.disable();
    if (taskGroupId) {
      const taskGroupName = taskGroupId.substring(taskGroupId.indexOf('#') + 1);
      this.taskGroupService.getTaskGroup(taskGroupName).subscribe(taskGroupDTO => {
        this.updateForm.patchValue({
          xQueryDiagnoseXML: taskGroupDTO.xQueryDiagnoseXML,
          xQuerySubmissionXML: taskGroupDTO.xQuerySubmissionXML,
          xQueryFileURL: taskGroupDTO.fileUrl,
        });
      });
    }
  }

  /**
   * Patches the values from a datalog task group in the update form
   * @param taskGroupId
   * @private
   */
  private patchDatalogTaskGroupValues(taskGroupId: string | undefined): void {
    if (taskGroupId) {
      const taskGroupName = taskGroupId.substring(taskGroupId.indexOf('#') + 1);
      this.taskGroupService.getTaskGroup(taskGroupName).subscribe(taskGroupDTO => {
        this.updateForm.patchValue({
          datalogFacts: taskGroupDTO.datalogFacts,
        });
      });
    }
  }
  private isSqlOrRaTask(taskAssignmentTypeId: string): boolean {
    return taskAssignmentTypeId === TaskAssignmentType.SQLTask.value || taskAssignmentTypeId === TaskAssignmentType.RATask.value;
  }

  /**
   * Sets all booleans indicating the task-type to false
   * @private
   */
  private setAllTaskTypeFlagsToFalse(): void {
    this.isSQLTask = false;
    this.isXQueryTask = false;
    this.isRATask = false;
    this.isDLQTask = false;
    this.isBpmnTask = false;
    this.isPmTask = false;
    this.isCalcTask = false;
    this.isOwlTask = false;
  }

  private clearAllTaskTypeDependentValidators(): void {
    this.updateForm.get('taskGroup')!.clearValidators();
    this.updateForm.get('taskGroup')!.updateValueAndValidity();
    this.updateForm.get('maxPoints')!.clearValidators();
    this.updateForm.get('maxPoints')!.updateValueAndValidity();
    this.updateForm.get('diagnoseLevelWeighting')!.clearValidators();
    this.updateForm.get('diagnoseLevelWeighting')!.updateValueAndValidity();
    this.updateForm.get('owlStatement')!.clearValidators();
    this.updateForm.get('owlStatement')!.updateValueAndValidity();
    this.updateForm.updateValueAndValidity();
  }

  private setMaxPointsRequired(): void {
    this.updateForm.get('maxPoints')!.setValidators(Validators.required);
    this.updateForm.get('maxPoints')!.updateValueAndValidity();
    this.updateForm.updateValueAndValidity();
  }

  private setDiagnoseLevelWeightingRequired(): void {
    this.updateForm.get('diagnoseLevelWeighting')!.setValidators(Validators.required);
    this.updateForm.get('diagnoseLevelWeighting')!.updateValueAndValidity();
    this.updateForm.updateValueAndValidity();
  }

  private setTaskGroupRequired(): void {
    this.updateForm.get('taskGroup')!.setValidators(Validators.required);
    this.updateForm.get('taskGroup')!.updateValueAndValidity();
    this.updateForm.updateValueAndValidity();
  }

  private isDkeDispatcherTask(taskAssignmentType: string): boolean {
    return (
      taskAssignmentType === TaskAssignmentType.RATask.value ||
      taskAssignmentType === TaskAssignmentType.SQLTask.value ||
      taskAssignmentType === TaskAssignmentType.DatalogTask.value ||
      taskAssignmentType === TaskAssignmentType.XQueryTask.value
      //taskAssignmentType === TaskAssignmentType.PmTask.value
    );
  }
}
