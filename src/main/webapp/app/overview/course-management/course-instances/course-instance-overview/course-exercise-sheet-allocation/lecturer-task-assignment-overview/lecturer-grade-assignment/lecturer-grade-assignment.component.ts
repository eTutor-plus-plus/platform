import { Component, OnInit } from '@angular/core';
import { LecturerTaskAssignmentService } from '../lecturer-task-assignment.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ILecturerGradingInfo, ILecturerStudentTaskAssignmentInfoModel } from '../lecturer-task-assignment.model';

/**
 * Modal component for displaying
 */
@Component({
  selector: 'jhi-lecturer-grade-assignment',
  templateUrl: './lecturer-grade-assignment.component.html',
})
export class LecturerGradeAssignmentComponent implements OnInit {
  private _lecturerStudentInfoModel?: ILecturerStudentTaskAssignmentInfoModel;

  public availableGradingInfos: ILecturerGradingInfo[] = [];
  public selectedGradingInfo?: ILecturerGradingInfo;
  public currentIndex = 0;

  /**
   * Constructor.
   *
   * @param lecturerTaskService the injected lecturer task assignment service
   * @param activeModal the injected active modal service
   */
  constructor(private lecturerTaskService: LecturerTaskAssignmentService, private activeModal: NgbActiveModal) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}

  /**
   * Sets the lecturer student info model.
   *
   * @param value the value to set
   */
  public set lecturerStudentInfoModel(value: ILecturerStudentTaskAssignmentInfoModel) {
    this._lecturerStudentInfoModel = value;

    // TODO: Implement loading
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
}
