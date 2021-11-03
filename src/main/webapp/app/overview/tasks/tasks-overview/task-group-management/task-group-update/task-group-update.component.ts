import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators } from '@angular/forms';
import { TaskGroupManagementService } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.service';
import { ITaskGroupDTO, TaskGroupType } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.model';
import { SqlExerciseService } from 'app/overview/dispatcher/services/sql-exercise.service';

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
  public isSQLTask = false;
  public readonly taskGroupTypes = TaskGroupType.Values;
  public taskGroupToEdit?: ITaskGroupDTO;
  public editorOptions = { theme: 'vs-light', language: 'sql' };

  public taskGroup = this.fb.group({
    name: ['', [Validators.required]],
    description: ['', []],
    taskGroupType: [this.taskGroupTypes[0], [Validators.required]],
    sqlCreateStatements: ['', []],
    sqlInsertStatementsSubmission: ['', []],
    sqlInsertStatementsDiagnose: ['', []],
  });

  /**
   * Constructor.
   *
   * @param activeModal the injected active modal service
   * @param fb the injected form builder service
   * @param taskGroupService the injected task group service
   */
  constructor(
    private activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private taskGroupService: TaskGroupManagementService,
    private sqlExerciseService: SqlExerciseService
  ) {}

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
        sqlCreateStatements: this.taskGroupToEdit.sqlCreateStatements,
        sqlInsertStatementsSubmission: this.taskGroupToEdit.sqlInsertStatementsSubmission,
        sqlInsertStatementsDiagnose: this.taskGroupToEdit.sqlInsertStatementsDiagnose,
      });
      if (this.taskGroupToEdit.taskGroupTypeId === TaskGroupType.SQLType.value) {
        this.isSQLTask = true;
      }
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
    const taskGroupTypeId = (this.taskGroup.get(['taskGroupType'])!.value as TaskGroupType).value;
    const sqlCreateStatements = this.taskGroup.get(['sqlCreateStatements'])!.value as string | undefined;
    const sqlInsertStatementsSubmission = this.taskGroup.get(['sqlInsertStatementsSubmission'])!.value as string | undefined;
    const sqlInsertStatementsDiagnose = this.taskGroup.get(['sqlInsertStatementsDiagnose'])!.value as string | undefined;
    try {
      if (this.isNew) {
        const newTaskGroup = await this.taskGroupService
          .createNewTaskGroup({
            name,
            description,
            taskGroupTypeId,
            sqlCreateStatements,
            sqlInsertStatementsSubmission,
            sqlInsertStatementsDiagnose,
          })
          .toPromise();

        this.isSaving = false;
        this.activeModal.close(newTaskGroup);
      } else {
        this.taskGroupToEdit!.description = description;
        this.taskGroupToEdit!.sqlCreateStatements = sqlCreateStatements;
        this.taskGroupToEdit!.sqlInsertStatementsSubmission = sqlInsertStatementsSubmission;
        this.taskGroupToEdit!.sqlInsertStatementsDiagnose = sqlInsertStatementsDiagnose;

        const taskFromService = await this.taskGroupService.modifyTaskGroup(this.taskGroupToEdit!).toPromise();

        this.isSaving = false;
        this.activeModal.close(taskFromService);
      }

      if (
        taskGroupTypeId === TaskGroupType.SQLType.value &&
        sqlCreateStatements &&
        sqlInsertStatementsDiagnose &&
        sqlInsertStatementsSubmission
      ) {
        this.sqlExerciseService
          .executeDDL(name, sqlCreateStatements, sqlInsertStatementsSubmission, sqlInsertStatementsDiagnose)
          .subscribe();
      }
    } catch (e) {
      this.isSaving = false;
      throw e;
    }
  }

  public groupTypeChanged(): void {
    const groupType = (this.taskGroup.get(['taskGroupType'])!.value as TaskGroupType).value;
    groupType === TaskGroupType.SQLType.value ? (this.isSQLTask = true) : (this.isSQLTask = false);
  }
}
