import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TasksService } from '../../tasks.service';
import { ITaskModel, TaskDifficulty } from '../task.model';
import { URL_OR_EMPTY_PATTERN } from '../../../../shared/constants/input.constants';

/**
 * Component for creating / updating tasks.
 */
@Component({
  selector: 'jhi-task-update',
  templateUrl: './task-update.component.html',
})
export class TaskUpdateComponent implements OnInit {
  private _taskModel?: ITaskModel;

  public isSaving = false;
  public readonly difficulties = TaskDifficulty.Values;

  public readonly updateForm = this.fb.group({
    header: ['', [Validators.required]],
    creator: ['', [Validators.required]],
    organisationUnit: ['', [Validators.required]],
    privateTask: [false],
    taskDifficulty: [this.difficulties[0], [Validators.required]],
    processingTime: [''],
    url: ['', [Validators.pattern(URL_OR_EMPTY_PATTERN)]],
    instruction: [''],
  });

  /**
   * Constructor.
   *
   * @param fb the injected form builder service
   * @param activeModal the injected active modal service
   * @param tasksService the injected tasks service
   */
  constructor(private fb: FormBuilder, private activeModal: NgbActiveModal, private tasksService: TasksService) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}

  /**
   * Saves the task.
   */
  public save(): void {
    this.isSaving = true;
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
