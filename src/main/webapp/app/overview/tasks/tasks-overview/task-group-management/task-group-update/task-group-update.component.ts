import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators } from '@angular/forms';
import { TaskGroupManagementService } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.service';
import { ITaskGroupDTO } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.model';

/**
 * Component for adding / manipulation task groups.
 */
@Component({
  selector: 'jhi-task-group-update',
  templateUrl: './task-group-update.component.html',
})
export class TaskGroupUpdateComponent {
  public isNew = true;
  public isSaving = false;
  public taskGroupToEdit?: ITaskGroupDTO;

  public taskGroup = this.fb.group({
    name: ['', [Validators.required]],
    description: ['', []],
  });

  /**
   * Constructor.
   *
   * @param activeModal the injected active modal service
   * @param fb the injected form builder service
   * @param taskGroupService the injected task group service
   */
  constructor(private activeModal: NgbActiveModal, private fb: FormBuilder, private taskGroupService: TaskGroupManagementService) {}

  /**
   * Sets the task id => edit mode.
   *
   * @param value the value to set
   */
  @Input()
  public set taskId(value: string) {
    (async () => {
      const name = value.substr(value.lastIndexOf('#') + 1);

      this.taskGroupToEdit = await this.taskGroupService.getTaskGroup(name).toPromise();
      this.taskGroup.patchValue({
        name: this.taskGroupToEdit.name,
        description: this.taskGroupToEdit.description,
      });
      this.isNew = false;
    })();
  }

  /**
   * Closes the modal dialog.
   */
  public close(): void {
    this.activeModal.dismiss();
  }

  /**
   * Asynchronously saves the task group.
   */
  public async saveAsync(): Promise<void> {
    this.isSaving = true;
    const name = this.taskGroup.get(['name'])!.value as string;
    const description = this.taskGroup.get(['description'])!.value as string | undefined;

    try {
      if (this.isNew) {
        const newTaskGroup = await this.taskGroupService
          .createNewTaskGroup({
            name,
            description,
          })
          .toPromise();

        this.isSaving = false;
        this.activeModal.close(newTaskGroup);
      } else {
        this.taskGroupToEdit!.description = description;

        const taskFromService = await this.taskGroupService.modifyTaskGroup(this.taskGroupToEdit!).toPromise();

        this.isSaving = false;
        this.activeModal.close(taskFromService);
      }
    } catch (e) {
      this.isSaving = false;
      throw e;
    }
  }
}
