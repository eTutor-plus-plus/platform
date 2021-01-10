import { Component, OnInit } from '@angular/core';
import { ITaskDisplayModel, ITaskModel } from '../../task.model';
import { TasksService } from '../../tasks.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

/**
 * Component for displaying a task.
 */
@Component({
  selector: 'jhi-task-display',
  templateUrl: './task-display.component.html',
})
export class TaskDisplayComponent implements OnInit {
  private _taskDisplayModel?: ITaskDisplayModel;
  private _taskModel?: ITaskModel;

  /**
   * Constructor.
   *
   * @param tasksService the injected task service
   * @param activeModal the active modal service
   */
  constructor(private tasksService: TasksService, private activeModal: NgbActiveModal) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}

  /**
   * Returns the task display model.
   */
  public get taskDisplayModel(): ITaskDisplayModel | undefined {
    return this._taskDisplayModel;
  }

  /**
   * Sets the task display model.
   *
   * @param value the value to set.
   */
  public set taskDisplayModel(value: ITaskDisplayModel | undefined) {
    this._taskDisplayModel = value;

    if (value) {
      this.tasksService.getTaskAssignmentById(value.taskId).subscribe(response => {
        if (response.body) {
          this._taskModel = response.body;
        }
      });
    }
  }

  /**
   * Returns the task model.
   */
  public get taskModel(): ITaskModel | undefined {
    return this._taskModel;
  }

  /**
   * Closes the modal dialog window.
   */
  public close(): void {
    this.activeModal.close();
  }
}
