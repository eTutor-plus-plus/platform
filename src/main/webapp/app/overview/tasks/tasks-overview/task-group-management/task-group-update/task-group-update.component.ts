import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators } from '@angular/forms';
import { TaskGroupManagementService } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.service';
import { ITaskGroupDTO, TaskGroupType } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.model';
import { HttpClient } from '@angular/common/http';
import { values } from 'lodash';
import {lastValueFrom} from "rxjs";

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
  public isDLGGroup = false;
  public isFDGroup = false;
  public readonly taskGroupTypes = TaskGroupType.Values;
  public taskGroupToEdit?: ITaskGroupDTO;
  public editorOptions = { theme: 'vs-light', language: 'pgsql' };
  public editorOptionsXML = { theme: 'vs-light', language: 'xml' };
  public editorOptionsDLG = { theme: 'datalog-light', language: 'datalog' };
  public editorOptionsFD = { theme: 'vs-light', language: 'fd' };

  public taskGroup = this.fb.group({
    name: ['', [Validators.required]],
    description: ['', []],
    taskGroupType: [this.taskGroupTypes[0], [Validators.required]],
    sqlCreateStatements: ['', []],
    sqlInsertStatementsSubmission: ['', []],
    sqlInsertStatementsDiagnose: ['', []],
    diagnoseXML: ['', []],
    submissionXML: ['', []],
    datalogFacts: ['', []],
    fDependencies: ['', []],
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
    private http: HttpClient
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

      this.taskGroupToEdit = await lastValueFrom(this.taskGroupService.getTaskGroup(name));

      this.taskGroup.patchValue({
        name: this.taskGroupToEdit!.name,
        description: this.taskGroupToEdit!.description,
        sqlCreateStatements: this.taskGroupToEdit!.sqlCreateStatements,
        sqlInsertStatementsSubmission: this.taskGroupToEdit!.sqlInsertStatementsSubmission,
        sqlInsertStatementsDiagnose: this.taskGroupToEdit!.sqlInsertStatementsDiagnose,
        diagnoseXML: this.taskGroupToEdit!.xQueryDiagnoseXML,
        submissionXML: this.taskGroupToEdit!.xQuerySubmissionXML,
        datalogFacts: this.taskGroupToEdit!.datalogFacts,
        fDependencies: this.taskGroupToEdit!.fDependencies,
        taskGroupType: TaskGroupType.getTaskGroup(this.taskGroupToEdit!.taskGroupTypeId),
      });
      if (this.taskGroupToEdit.taskGroupTypeId === TaskGroupType.SQLType.value) {
        this.adjustFormForSQLType();
      } else if (this.taskGroupToEdit.taskGroupTypeId === TaskGroupType.XQueryType.value) {
        this.adjustFormForXQType();
      } else if (this.taskGroupToEdit.taskGroupTypeId === TaskGroupType.DatalogType.value) {
        this.adjustFormForDLGType();
      } else if (this.taskGroupToEdit.taskGroupTypeId === TaskGroupType.FDType.value) {
        this.adjustFormForFDType();
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
    const datalogFacts = this.taskGroup.get(['datalogFacts'])!.value as string | undefined;
    const fDependencies = this.taskGroup.get(['fDependencies'])!.value as string | undefined;
    try {
      if (this.isNew) {
        const newTaskGroup = await lastValueFrom(this.taskGroupService
          .createNewTaskGroup({
            name,
            description,
            taskGroupTypeId,
            sqlCreateStatements,
            sqlInsertStatementsSubmission,
            sqlInsertStatementsDiagnose,
            xQueryDiagnoseXML,
            xQuerySubmissionXML,
            datalogFacts,
            fDependencies,
          }));

        this.isSaving = false;
        this.activeModal.close(newTaskGroup);
      } else {
        this.taskGroupToEdit!.description = description;
        this.taskGroupToEdit!.sqlCreateStatements = sqlCreateStatements;
        this.taskGroupToEdit!.sqlInsertStatementsSubmission = sqlInsertStatementsSubmission;
        this.taskGroupToEdit!.sqlInsertStatementsDiagnose = sqlInsertStatementsDiagnose;
        this.taskGroupToEdit!.xQueryDiagnoseXML = xQueryDiagnoseXML;
        this.taskGroupToEdit!.xQuerySubmissionXML = xQuerySubmissionXML;
        this.taskGroupToEdit!.datalogFacts = datalogFacts;
        this.taskGroupToEdit!.fDependencies = fDependencies;
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
    const groupType = (this.taskGroup.get(['taskGroupType'])?.value as TaskGroupType).value;

    if (groupType === TaskGroupType.DatalogType.value) {
      this.adjustFormForDLGType();
    } else {
      this.isDLGGroup = false;
    }

    if (groupType === TaskGroupType.SQLType.value) {
      this.adjustFormForSQLType();
    } else {
      this.isSQLGroup = false;
    }

    if (groupType === TaskGroupType.XQueryType.value) {
      this.adjustFormForXQType();
    } else {
      this.isXQueryGroup = false;
    }

    if (groupType === TaskGroupType.FDType.value) {
      this.adjustFormForFDType();
    } else {
      this.isFDGroup = false;
    }
  }

  private adjustFormForDLGType(): void {
    this.isDLGGroup = true;
    this.taskGroup.get(['datalogFacts'])?.setValidators(Validators.required);
    this.taskGroup.get(['datalogFacts'])?.updateValueAndValidity();
    this.taskGroup.updateValueAndValidity();
    this.clearSQLValidators();
    this.clearFDAdjustment();
  }

  private adjustFormForSQLType(): void {
    this.isSQLGroup = true;
    this.taskGroup.get(['sqlCreateStatements'])?.setValidators(Validators.required);
    this.taskGroup.get(['sqlCreateStatements'])?.updateValueAndValidity();
    this.taskGroup.updateValueAndValidity();
    this.clearDLGValidators();
    this.clearFDAdjustment();
  }

  private adjustFormForXQType(): void {
    this.isXQueryGroup = true;
    this.clearDLGValidators();
    this.clearSQLValidators();
    this.clearFDAdjustment();
  }

  private adjustFormForFDType(): void {
    this.isFDGroup = true;
    if (!this.taskGroupToEdit) {
      this.taskGroupService.nextFdID().then(body => {
        this.taskGroup.get(['name'])?.setValue('FunctionalDependencies-' + body);
        this.taskGroup.get(['name'])?.disable();
      });
    }
    this.taskGroup.get(['fDependencies'])?.setValidators(Validators.required);
    this.taskGroup.get(['fDependencies'])?.updateValueAndValidity();
    this.clearDLGValidators();
    this.clearSQLValidators();
  }
  private clearFDAdjustment(): void {
    this.taskGroup.get(['name'])?.setValue('');
    this.taskGroup.get(['name'])?.enable();
    this.taskGroup.get(['fDependencies'])?.clearValidators();
    this.taskGroup.get(['fDependencies'])?.updateValueAndValidity();
  }

  private clearSQLValidators(): void {
    this.taskGroup.get(['sqlCreateStatements'])?.clearValidators();
    this.taskGroup.get(['sqlCreateStatements'])?.updateValueAndValidity();
    this.taskGroup.updateValueAndValidity();
  }

  private clearDLGValidators(): void {
    this.taskGroup.get(['datalogFacts'])?.clearValidators();
    this.taskGroup.get(['datalogFacts'])?.updateValueAndValidity();
    this.taskGroup.updateValueAndValidity();
  }
}
