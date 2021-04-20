import { Component } from '@angular/core';
import { LecturerTaskAssignmentService } from '../lecturer-task-assignment.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { IGradingInfoVM, ILecturerGradingInfo, ILecturerStudentTaskAssignmentInfoModel } from '../lecturer-task-assignment.model';

/**
 * Modal component for displaying
 */
@Component({
  selector: 'jhi-lecturer-grade-assignment',
  templateUrl: './lecturer-grade-assignment.component.html',
})
export class LecturerGradeAssignmentComponent {
  public availableGradingInfos: ILecturerGradingInfo[] = [];
  public selectedGradingInfo?: ILecturerGradingInfo;
  public currentIndex = 0;
  public isSaving = false;

  private _lecturerStudentInfoModel?: ILecturerStudentTaskAssignmentInfoModel;

  /**
   * Constructor.
   *
   * @param lecturerTaskService the injected lecturer task assignment service
   * @param activeModal the injected active modal service
   */
  constructor(private lecturerTaskService: LecturerTaskAssignmentService, private activeModal: NgbActiveModal) {}

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

  /**
   * Asynchronously loads the grading info.
   */
  private async loadGradingInfosAsync(): Promise<any> {
    const response = await this.lecturerTaskService.getGradingInfo(this.lecturerStudentInfoModel).toPromise();
    this.availableGradingInfos = response.body ?? [];

    if (this.availableGradingInfos.length > 0) {
      this.selectedGradingInfo = this.availableGradingInfos[0];
      this.currentIndex = 0;
    }
  }
}
