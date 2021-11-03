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
import { LecturerRunSubmissionComponent } from '../../../dispatcher/lecturer-run-submission/lecturer-run-submission.component';

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
  public editorOptionsReadOnly = { theme: 'vs-light', language: 'pgsql', readOnly: true };
  public editorOptionsXMLReadOnly = { theme: 'vs-light', language: 'xml', readOnly: true };
  public isSQLTask = false;
  public isRATask = false;
  public isXQueryTask = false;
  public taskGroups: ITaskGroupDisplayDTO[] = [];

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
    maxPoints: [''],
    diagnoseLevelWeighting: [''],
    processingTime: [''],
    url: ['', [Validators.pattern(URL_OR_EMPTY_PATTERN)]],
    instruction: [''],
    taskGroup: ['', []],
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
   */
  constructor(
    private fb: FormBuilder,
    private activeModal: NgbActiveModal,
    private modalService: NgbModal,
    private tasksService: TasksService,
    private eventManager: EventManager,
    private sqlExerciseService: SqlExerciseService,
    private taskGroupService: TaskGroupManagementService
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
    };

    const urlStr: string = this.updateForm.get('url')!.value;
    if (urlStr) {
      newTask.url = new URL(urlStr);
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

    const maxPoints: string = this.updateForm.get('maxPoints')!.value;
    if (maxPoints) {
      newTask.maxPoints = maxPoints;
    }

    const diagnoseLevelWeighting: string = this.updateForm.get('diagnoseLevelWeighting')!.value;
    if (diagnoseLevelWeighting) {
      newTask.diagnoseLevelWeighting = diagnoseLevelWeighting;
    }
    const processingTime: string = this.updateForm.get('processingTime')!.value;
    if (processingTime.trim()) {
      newTask.processingTime = processingTime.trim();
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
        maxPoints: newTask.maxPoints,
        diagnoseLevelWeighting: newTask.diagnoseLevelWeighting,
        processingTime: newTask.processingTime,
        url: newTask.url,
        instruction: newTask.instruction,
        privateTask: newTask.privateTask,
        taskAssignmentTypeId: newTask.taskAssignmentTypeId,
        taskGroupId: newTask.taskGroupId,
        creationDate: this.taskModel!.creationDate,
        id: this.taskModel!.id,
        internalCreator: this.taskModel!.internalCreator,
        learningGoalIds: this.taskModel!.learningGoalIds,
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
      const sqlSolution = value.sqlSolution;
      const xQuerySolution = value.xQuerySolution;
      const xQueryXPathSorting = value.xQueryXPathSorting;
      const maxPoints = value.maxPoints ?? '';
      const diagnoseLevelWeighting = value.diagnoseLevelWeighting ?? '';
      const processingTime = value.processingTime ?? '';
      const url = value.url ? value.url.toString() : '';
      const instruction = value.instruction ?? '';
      const taskGroupId = value.taskGroupId ?? '';
      const taskAssignmentTypeId = value.taskAssignmentTypeId;

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
        maxPoints,
        diagnoseLevelWeighting,
        processingTime,
        url,
        instruction,
        taskGroup: value.taskGroupId ?? '',
      });
      this.taskTypeChanged();
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
      this.patchSQLValues(taskGroupId);
    } else if (taskAssignmentTypeId === TaskAssignmentType.XQueryTask.value) {
      this.isXQueryTask = true;
      this.patchXQueryValues(taskGroupId);
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
    if (this.isSqlOrRaTask(taskAssignmentTypeId) || taskAssignmentTypeId === TaskAssignmentType.XQueryTask.value) {
      if (taskAssignmentTypeId === TaskAssignmentType.SQLTask.value) {
        this.isSQLTask = true;
        this.isXQueryTask = false;
        this.isRATask = false;
      } else if (taskAssignmentTypeId === TaskAssignmentType.XQueryTask.value) {
        this.isXQueryTask = true;
        this.isSQLTask = false;
        this.isRATask = false;
      } else {
        this.isRATask = true;
        this.isSQLTask = false;
        this.isXQueryTask = false;
      }
      this.updateForm.get('maxPoints')!.setValidators(Validators.required);
      this.updateForm.get('diagnoseLevelWeighting')!.setValidators(Validators.required);
      this.updateForm.updateValueAndValidity();
    } else {
      this.isSQLTask = false;
      this.isRATask = false;
      this.isXQueryTask = false;
      this.updateForm.get('maxPoints')!.clearValidators();
      this.updateForm.get('diagnoseLevelWeighting')!.clearValidators();
      this.updateForm.updateValueAndValidity();
    }
  }
  /**
   * Reacts to a change of the taskGroup by patching the relevant data from the group in the update form
   */
  public taskGroupChanged(): void {
    const taskGroupId = this.updateForm.get(['taskGroup'])!.value as string | undefined;
    const taskT = (this.updateForm.get(['taskAssignmentType'])!.value as TaskAssignmentType).value;
    if (this.isSqlOrRaTask(taskT)) {
      this.patchSQLValues(taskGroupId);
    } else if (taskT === TaskAssignmentType.XQueryTask.value) {
      this.patchXQueryValues(taskGroupId);
    }
  }

  /**
   * Opens a lecturer-run-submission modal window to test a solution
   */
  public openSolutionRunnerWindow(): void {
    const modalRef = this.modalService.open(LecturerRunSubmissionComponent, { backdrop: 'static', size: 'xl' });
    let subm = '';
    const taskT = (this.updateForm.get(['taskAssignmentType'])!.value as TaskAssignmentType).value;
    if (taskT === TaskAssignmentType.SQLTask.value) {
      subm = this.updateForm.get(['sqlSolution'])?.value ?? '';
    } else if (taskT === TaskAssignmentType.XQueryTask.value) {
      subm = this.updateForm.get(['xQuerySolution'])?.value ?? '';
    }
    const id = this.updateForm.get(['taskIdForDispatcher'])!.value;
    (modalRef.componentInstance as LecturerRunSubmissionComponent).submissionEntry = {
      hasBeenSolved: false,
      isSubmitted: false,
      instant: '',
      submission: subm,
      dispatcherId: id,
      taskType: taskT,
    };
    (modalRef.componentInstance as LecturerRunSubmissionComponent).showHeader = false;
  }
  /**
   * Patches the values from an SQL-Task group in the update form
   * @param taskGroupId the task-group-id
   */
  private patchSQLValues(taskGroupId: string | undefined): void {
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
   * Patches the solution for an sql exercise in the update form
   * @param solution the solution to be patched
   * @private
   */
  private patchSQLSolution(solution: string): void {
    this.updateForm.patchValue({
      sqlSolution: solution,
    });
  }

  private isSqlOrRaTask(taskAssignmentTypeId: string): boolean {
    return taskAssignmentTypeId === TaskAssignmentType.SQLTask.value || taskAssignmentTypeId === TaskAssignmentType.RATask.value;
  }

  private patchXQueryValues(taskGroupId: string | undefined): void {
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
}
