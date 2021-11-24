import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators } from '@angular/forms';
import { TaskGroupManagementService } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.service';
import { ITaskGroupDTO, TaskGroupType } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.model';

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
  public isSQLGroup = false;
  public isXQueryGroup = false;
  public readonly taskGroupTypes = TaskGroupType.Values;
  public taskGroupToEdit?: ITaskGroupDTO;
  public editorOptions = { theme: 'vs-light', language: 'pgsql' };
  public editorOptionsXML = { theme: 'vs-light', language: 'xml' };

  public taskGroup = this.fb.group({
    name: ['', [Validators.required]],
    description: ['', []],
    taskGroupType: [this.taskGroupTypes[0], [Validators.required]],
    sqlCreateStatements: ['', []],
    sqlInsertStatementsSubmission: ['', []],
    sqlInsertStatementsDiagnose: ['', []],
    diagnoseXML: ['', []],
    submissionXML: ['', []],
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
        sqlCreateStatements: this.taskGroupToEdit.sqlCreateStatements,
        sqlInsertStatementsSubmission: this.taskGroupToEdit.sqlInsertStatementsSubmission,
        sqlInsertStatementsDiagnose: this.taskGroupToEdit.sqlInsertStatementsDiagnose,
        diagnoseXML: this.taskGroupToEdit.xQueryDiagnoseXML,
        submissionXML: this.taskGroupToEdit.xQuerySubmissionXML,
      });
      if (this.taskGroupToEdit.taskGroupTypeId === TaskGroupType.SQLType.value) {
        this.isSQLGroup = true;
      } else if (this.taskGroupToEdit.taskGroupTypeId === TaskGroupType.XQueryType.value) {
        this.isXQueryGroup = true;
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
    const xQueryDiagnoseXML = this.taskGroup.get(['diagnoseXML'])!.value as string | undefined;
    const xQuerySubmissionXML = this.taskGroup.get(['submissionXML'])!.value as string | undefined;
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
            xQueryDiagnoseXML,
            xQuerySubmissionXML,
          })
          .toPromise();

        this.isSaving = false;
        this.activeModal.close(newTaskGroup);
      } else {
        this.taskGroupToEdit!.description = description;
        this.taskGroupToEdit!.sqlCreateStatements = sqlCreateStatements;
        this.taskGroupToEdit!.sqlInsertStatementsSubmission = sqlInsertStatementsSubmission;
        this.taskGroupToEdit!.sqlInsertStatementsDiagnose = sqlInsertStatementsDiagnose;
        this.taskGroupToEdit!.xQueryDiagnoseXML = xQueryDiagnoseXML;
        this.taskGroupToEdit!.xQuerySubmissionXML = xQuerySubmissionXML;

        const taskFromService = await this.taskGroupService.modifyTaskGroup(this.taskGroupToEdit!).toPromise();

        this.isSaving = false;
        this.activeModal.close(taskFromService);
      }
    } catch (e) {
      this.isSaving = false;
      throw e;
    }
  }

  public groupTypeChanged(): void {
    const groupType = (this.taskGroup.get(['taskGroupType'])!.value as TaskGroupType).value;
    groupType === TaskGroupType.SQLType.value ? (this.isSQLGroup = true) : (this.isSQLGroup = false);
    groupType === TaskGroupType.XQueryType.value ? (this.isXQueryGroup = true) : (this.isXQueryGroup = false);
  }
}
