import { Component, OnInit } from '@angular/core';
import { ILecturerTaskAssignmentInfoModel, IStudentAssignmentOverviewInfo } from './lecturer-task-assignment.model';
import { LecturerTaskAssignmentService } from './lecturer-task-assignment.service';
import { COUNT_HEADER, ITEMS_PER_PAGE } from '../../../../../../shared/constants/pagination.constants';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

/**
 * Modal window component for displaying the completed student assignments.
 */
@Component({
  selector: 'jhi-lecturer-task-assignment-overview',
  templateUrl: './lecturer-task-assignment-overview.component.html',
})
export class LecturerTaskAssignmentOverviewComponent implements OnInit {
  private _assignedSheetInfo?: ILecturerTaskAssignmentInfoModel;

  public page = 1;
  public itemsPerPage: number;
  public totalItems = 0;
  public entries: IStudentAssignmentOverviewInfo[] = [];

  /**
   * Constructor.
   *
   * @param lecturerAssignmentService the injected lecturer task assignment service
   * @param activeModal the injected active modal service
   * @param modalService the injected modal service
   */
  constructor(
    private lecturerAssignmentService: LecturerTaskAssignmentService,
    private activeModal: NgbActiveModal,
    private modalService: NgbModal
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}

  /**
   * Sets the assigned sheet info.
   *
   * @param value the value to set
   */
  public set assignedSheetInfo(value: ILecturerTaskAssignmentInfoModel) {
    this._assignedSheetInfo = value;
    this.transition();
  }

  /**
   * Returns the assigned sheet info.
   */
  public get assignedSheetInfo(): ILecturerTaskAssignmentInfoModel {
    return this._assignedSheetInfo!;
  }

  /**
   * Performs the page transition.
   */
  public transition(): void {
    this.loadPageAsync();
  }

  /**
   * Returns the identity of the given item.
   *
   * @param index the index
   * @param item the item
   */
  public trackIdentity(index: number, item: IStudentAssignmentOverviewInfo): string {
    return item.matriculationNo;
  }

  /**
   * Closes the modal window.
   */
  public close(): void {
    this.activeModal.close();
  }

  /**
   * Opens the grading window for the given student.
   *
   * @param entry the student assignment overview info
   */
  public grade(entry: IStudentAssignmentOverviewInfo): void {
    // TODO: Implement
  }

  /**
   * Asynchronously loads the page.
   */
  private async loadPageAsync(): Promise<any> {
    const response = await this.lecturerAssignmentService
      .getStudentAssignmentInfoPage(this.assignedSheetInfo, {
        page: this.page - 1,
        size: this.itemsPerPage,
        sort: [],
      })
      .toPromise();

    this.totalItems = Number(response.headers.get(COUNT_HEADER));
    this.entries = response.body ?? [];
  }
}
