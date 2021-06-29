import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TasksService } from '../../tasks.service';
import { INewTaskModel, ITaskModel, TaskAssignmentType, TaskDifficulty } from '../../task.model';
import { CustomValidators } from 'app/shared/validators/custom-validators';
import { URL_OR_EMPTY_PATTERN } from 'app/config/input.constants';
import { EventManager } from 'app/core/util/event-manager.service';
import { ITaskGroupDisplayDTO } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.model';
import { TaskGroupManagementService } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.service';

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

  public taskGroups: ITaskGroupDisplayDTO[] = [];

  public readonly updateForm = this.fb.group({
    header: ['', [CustomValidators.required]],
    creator: ['', [CustomValidators.required]],
    organisationUnit: ['', [CustomValidators.required]],
    privateTask: [false],
    taskDifficulty: [this.difficulties[0], [Validators.required]],
    taskAssignmentType: [this.taskTypes[0], [Validators.required]],
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
   * @param tasksService the injected tasks service
   * @param eventManager the injected event manager service
   * @param taskGroupService the task group service
   */
  constructor(
    private fb: FormBuilder,
    private activeModal: NgbActiveModal,
    private tasksService: TasksService,
    private eventManager: EventManager,
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
      const processingTime = value.processingTime ?? '';
      const url = value.url ? value.url.toString() : '';
      const instruction = value.instruction ?? '';

      this.updateForm.patchValue({
        header: value.header,
        creator: value.creator,
        organisationUnit: value.organisationUnit,
        privateTask: value.privateTask,
        taskDifficulty,
        processingTime,
        url,
        instruction,
        taskGroup: value.taskGroupId ?? '',
      });
    }
  }

  /**
   * Returns the task model
   */
  public get taskModel(): ITaskModel | undefined {
    return this._taskModel;
  }

  /**
   * Returns whether this modal window is in new mode.
   * {@code true} = new mode, {@code false} = edit mode
   */
  public get isNew(): boolean {
    return this._taskModel === undefined;
  }
}
