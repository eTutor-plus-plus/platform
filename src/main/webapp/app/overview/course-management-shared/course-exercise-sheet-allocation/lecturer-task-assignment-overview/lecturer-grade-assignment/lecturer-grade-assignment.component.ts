import { Component } from '@angular/core';
import { LecturerTaskAssignmentService } from '../lecturer-task-assignment.service';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { IGradingInfoVM, ILecturerGradingInfo, ILecturerStudentTaskAssignmentInfoModel } from '../lecturer-task-assignment.model';
import { TaskAssignmentType } from 'app/overview/tasks/task.model';
import { TaskSubmissionsComponent } from '../../../../dispatcher/task-submissions/task-submissions.component';
import { StudentService } from '../../../../shared/students/student-service';
import { TaskSubmissionsModel } from '../../../../dispatcher/task-submissions/task-submissions.model';

/**
 * Modal component for displaying
 */
@Component({
  selector: 'jhi-lecturer-grade-assignment',
  templateUrl: './lecturer-grade-assignment.component.html',
})
export class LecturerGradeAssignmentComponent {
  public availableGradingInfos: ILecturerGradingInfo[] = [];
  public currentIndex = 0;
  public isSaving = false;
  public currentFile = -1;

  private _lecturerStudentInfoModel?: ILecturerStudentTaskAssignmentInfoModel;
  private _selectedGradingInfo?: ILecturerGradingInfo;

  /**
   * Constructor.
   *
   * @param lecturerTaskService the injected lecturer task assignment service
   * @param activeModal the injected active modal service
   * @param modalService the injected modal service
   */
  constructor(
    private lecturerTaskService: LecturerTaskAssignmentService,
    private activeModal: NgbActiveModal,
    private modalService: NgbModal,
    private studentService: StudentService
  ) {}

  /**
   * Sets the selected grading info.
   *
   * @param value the value to set
   */
  public set selectedGradingInfo(value: ILecturerGradingInfo | undefined) {
    this._selectedGradingInfo = value;

    if (value && value.taskTypeId === TaskAssignmentType.UploadTask.value) {
      (async () => {
        const exerciseSheetUUID = this.lecturerStudentInfoModel.exerciseSheetId.substr(
          this.lecturerStudentInfoModel.exerciseSheetId.lastIndexOf('#') + 1
        );

        this.currentFile = await this.lecturerTaskService
          .getFileIdOfStudentsAssignment(
            this.lecturerStudentInfoModel.courseInstanceId,
            exerciseSheetUUID,
            value.orderNo,
            this.lecturerStudentInfoModel.matriculationNo
          )
          .toPromise();
      })();
    } else {
      this.currentFile = -1;
    }
  }

  /**
   * Returns the selected grading info.
   */
  public get selectedGradingInfo(): ILecturerGradingInfo | undefined {
    return this._selectedGradingInfo;
  }

  /**
   * Sets the lecturer student info model.
   *
   * @param value the value to set
   */
  public set lecturerStudentInfoModel(value: ILecturerStudentTaskAssignmentInfoModel) {
    this._lecturerStudentInfoModel = value;

    this.loadGradingInfosAsync();
  }

  /**
   * Returns the lecturer student info model.
   */
  public get lecturerStudentInfoModel(): ILecturerStudentTaskAssignmentInfoModel {
    return this._lecturerStudentInfoModel!;
  }

  /**
   * Selects the next grading info.
   */
  public selectNextGradingInfo(): void {
    if (this.isNextGradingInfoAvailable()) {
      this.currentIndex++;
      this.selectedGradingInfo = this.availableGradingInfos[this.currentIndex];
    }
  }

  /**
   * Selects the previous grading info.
   */
  public selectPreviousGradingInfo(): void {
    if (this.isPreviousGradingInfoAvailable()) {
      this.currentIndex--;
      this.selectedGradingInfo = this.availableGradingInfos[this.currentIndex];
    }
  }

  /**
   * Returns whether a next grading info is available or not.
   */
  public isNextGradingInfoAvailable(): boolean {
    return this.currentIndex + 1 < this.availableGradingInfos.length;
  }

  /**
   * Returns whether a previous grading info is available or not.
   */
  public isPreviousGradingInfoAvailable(): boolean {
    return this.currentIndex - 1 >= 0;
  }

  /**
   * Closes the modal window.
   */
  public close(): void {
    this.activeModal.close();
  }

  /**
   * Saves the current assessment.
   */
  public saveCurrentAssessment(): void {
    this.isSaving = true;
    const gradingInfoVM: IGradingInfoVM = {
      courseInstanceUUID: this.lecturerStudentInfoModel.courseInstanceId.substr(
        this.lecturerStudentInfoModel.courseInstanceId.lastIndexOf('#') + 1
      ),
      exerciseSheetUUID: this.lecturerStudentInfoModel.exerciseSheetId.substr(
        this.lecturerStudentInfoModel.exerciseSheetId.lastIndexOf('#') + 1
      ),
      matriculationNo: this.lecturerStudentInfoModel.matriculationNo,
      orderNo: this.selectedGradingInfo!.orderNo,
      goalCompleted: this.selectedGradingInfo!.completed,
    };
    this.lecturerTaskService.setGradeForAssignment(gradingInfoVM).subscribe(
      () => {
        this.selectedGradingInfo!.graded = true;
        this.isSaving = false;
      },
      () => (this.isSaving = false)
    );
  }

  public async openSubmissions(): Promise<any> {
    const courseInstanceId = this._lecturerStudentInfoModel?.courseInstanceId ?? '';
    const exerciseSheetUUID =
      this._lecturerStudentInfoModel?.exerciseSheetId.substr(this.lecturerStudentInfoModel.exerciseSheetId.lastIndexOf('#') + 1) ?? '';
    const matriculationNo = this._lecturerStudentInfoModel?.matriculationNo ?? '';
    const orderNo = this._selectedGradingInfo?.orderNo;
    let submissions: TaskSubmissionsModel[] = [];
    if (orderNo) {
      submissions = await this.studentService
        .getAllSubmissionsForAssignment(courseInstanceId, exerciseSheetUUID, orderNo.toString(), matriculationNo)
        .toPromise();
    }

    const modalRef = this.modalService.open(TaskSubmissionsComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as TaskSubmissionsComponent).submissions = submissions;

    (modalRef.componentInstance as TaskSubmissionsComponent).courseInstanceUUID = this._lecturerStudentInfoModel?.courseInstanceId;
    (modalRef.componentInstance as TaskSubmissionsComponent).exerciseSheetUUID = this._lecturerStudentInfoModel?.exerciseSheetId;
    (modalRef.componentInstance as TaskSubmissionsComponent).matriculationNo = this._lecturerStudentInfoModel?.matriculationNo;
    (modalRef.componentInstance as TaskSubmissionsComponent).orderNo = this._selectedGradingInfo?.orderNo.toString();
  }

  public isDispatcherTask(): boolean {
    return (
      this._selectedGradingInfo?.taskTypeId === TaskAssignmentType.SQLTask.value ||
      this._selectedGradingInfo?.taskTypeId === TaskAssignmentType.RATask.value ||
      this._selectedGradingInfo?.taskTypeId === TaskAssignmentType.XQueryTask.value
    );
  }
  /**
   * Asynchronously loads the grading info.
   */
  private async loadGradingInfosAsync(): Promise<any> {
    const response = await this.lecturerTaskService.getGradingInfo(this.lecturerStudentInfoModel).toPromise();
    this.availableGradingInfos = response.body ?? [];
    this.availableGradingInfos = this.availableGradingInfos.filter(x => x.submitted);

    if (this.availableGradingInfos.length > 0) {
      this.selectedGradingInfo = this.availableGradingInfos[0];
      this.currentIndex = 0;
    }
  }
}
