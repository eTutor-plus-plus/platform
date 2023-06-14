import { Component, OnInit } from '@angular/core';
import { ITaskDisplayModel, ITaskModel, ITaskVersionModel, TaskAssignmentType, TaskDifficulty } from '../../task.model';
import { TasksService } from '../../tasks.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AccountService } from '../../../../core/auth/account.service';
import { getEditorOptionsForTaskTypeUrl } from '../../../dispatcher/monaco-config';

/**
 * Component for displaying a task.
 */
@Component({
  selector: 'jhi-task-display',
  templateUrl: './task-display.component.html',
})
export class TaskDisplayComponent implements OnInit {
  private _taskDisplayModel?: ITaskDisplayModel;
  private readonly difficulties = TaskDifficulty.Values;
  private readonly taskTypes = TaskAssignmentType.Values;
  private userLogin = '';
  public _taskVersions: ITaskVersionModel[] = [];
  public version?: ITaskModel;
  public currentIndex = 0;
  public editorOptions = { theme: 'vs-light', language: 'pgsql', readOnly: true };

  /**
   * Constructor.
   *
   * @param accountService the injected account service
   * @param tasksService the injected task service
   * @param activeModal the active modal service
   */
  constructor(private accountService: AccountService, private tasksService: TasksService, private activeModal: NgbActiveModal) {}

  ngOnInit(): void {
    this.userLogin = this.accountService.getLoginName() ?? '';
  }

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
      this.tasksService.getTaskVersionsById(value.taskId).subscribe(response => {
        if (response.body) {
          this._taskVersions = response.body;
          this.version = this._taskVersions[0].version;
          this.editorOptions = getEditorOptionsForTaskTypeUrl(this.version.taskAssignmentTypeId, true);
        }
      });
    }
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

  previousVersion() {
    this.currentIndex += 1;
    if (this._taskVersions && this.currentIndex >= 0) {
      this.version = this._taskVersions[this.currentIndex].version;
    }
  }

  nextVersion() {
    this.currentIndex -= 1;
    if (this._taskVersions && this.currentIndex < this._taskVersions.length) {
      this.version = this._taskVersions[this.currentIndex].version;
    }
  }

  public isCurrentUserAllowedToEditTask(): boolean {
    return this._taskDisplayModel?.internalCreator === this.userLogin;
  }

  public resetToCurrentVersion(): void {
    if (
      !confirm(
        'Do you really want to reset to this version? This will update the task. Note, however, that the assigned learning goals will not be updated.'
      )
    ) {
      return;
    }
    if (this.version) {
      // delete all versions after current version
      // ToDo: delete all versions after current version
      // update to current version
      this.tasksService.saveEditedTask(this.version, 'reset task to previous version').subscribe(response => {
        // reload versions and reset current version and index
        if (this._taskDisplayModel) {
          this.tasksService.getTaskVersionsById(this._taskDisplayModel.taskId).subscribe(response => {
            if (response.body) {
              this._taskVersions = response.body;
              this.currentIndex = 0;
              this.version = this._taskVersions[0].version;
            }
          });
        }
      });
    }
  }

  protected readonly TaskAssignmentType = TaskAssignmentType;
  protected readonly getEditorOptionsForTaskTypeUrl = getEditorOptionsForTaskTypeUrl;
}
