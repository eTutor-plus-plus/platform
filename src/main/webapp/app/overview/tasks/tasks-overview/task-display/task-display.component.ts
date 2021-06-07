import { Component } from '@angular/core';
import { ITaskDisplayModel, ITaskModel, TaskAssignmentType, TaskDifficulty } from '../../task.model';
import { TasksService } from '../../tasks.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

/**
 * Component for displaying a task.
 */
@Component({
  selector: 'jhi-task-display',
  templateUrl: './task-display.component.html',
})
export class TaskDisplayComponent {
  private _taskDisplayModel?: ITaskDisplayModel;
  private _taskModel?: ITaskModel;
  private readonly difficulties = TaskDifficulty.Values;
  private readonly taskTypes = TaskAssignmentType.Values;

  /**
   * Constructor.
   *
   * @param tasksService the injected task service
   * @param activeModal the active modal service
   */
  constructor(private tasksService: TasksService, private activeModal: NgbActiveModal) {}

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
   * Returns the task difficulty object for the given URL.
   *
   * @param taskDifficultyUrl the task difficulty url
   */
  public getTaskDifficultyForURL(taskDifficultyUrl: string): TaskDifficulty {
    return this.difficulties.find(x => x.value === taskDifficultyUrl)!;
  }

  /**
   * Returns the task type object for a given URL.
   *
   * @param taskTypeUrl the task type url
   */
  public getTaskTypeForURL(taskTypeUrl: string): TaskAssignmentType {
    return this.taskTypes.find(x => x.value === taskTypeUrl)!;
  }

  /**
   * Closes the modal dialog window.
   */
  public close(): void {
    this.activeModal.close();
  }
}
