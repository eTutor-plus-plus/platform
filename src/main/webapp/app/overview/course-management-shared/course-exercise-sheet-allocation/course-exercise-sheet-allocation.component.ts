import { Component } from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { IDisplayableCourseInstanceDTO } from '../../course-management/course-mangement.model';
import { FormBuilder } from '@angular/forms';
import { IExerciseSheetDisplayDTO } from '../../exercise-sheets/exercise-sheets.model';
import { ExerciseSheetsService } from '../../exercise-sheets/exercise-sheets.service';
import { CourseManagementService } from '../../course-management/course-management.service';
import { forkJoin } from 'rxjs';
import { LecturerTaskAssignmentOverviewComponent } from './lecturer-task-assignment-overview/lecturer-task-assignment-overview.component';
import { COUNT_HEADER, ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { LecturerTaskAssignmentService } from './lecturer-task-assignment-overview/lecturer-task-assignment.service';
import { TaskPointEntryModel } from '../../course-management/course-instances/course-instance-overview/course-exercise-sheet-allocation/task-point-entry.model';

/**
 * Modal window for displaying exercise sheet assignments.
 */
@Component({
  selector: 'jhi-course-exercise-sheet-allocation',
  templateUrl: './course-exercise-sheet-allocation.component.html',
  styleUrls: ['./course-exercise-sheet-allocation.component.scss'],
})
export class CourseExerciseSheetAllocationComponent {
  public readonly allocationGroup = this.fb.group({});
  public isSaving = false;

  public page = 1;
  public itemsPerPage: number;
  public totalItems = 0;
  public exerciseSheets: IExerciseSheetDisplayDTO[] = [];
  public selectedSheetsId: string[] = [];

  private _courseInstance?: IDisplayableCourseInstanceDTO;
  private _selectedSheetIdsToSave: string[] = [];
  private _closedExerciseSheets: string[] = [];
  private _exerciseSheetPointOverview: TaskPointEntryModel[] = [];

  /**
   * Constructor.
   *
   * @param activeModal the injected active modal service
   * @param fb the injected form builder
   * @param exerciseSheetService the injected exercise sheet service
   * @param courseService the injected course service
   * @param modalService the injected modal service
   * @param lecturerAssignmentService the injected lecturer assignment service
   */
  constructor(
    private activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private exerciseSheetService: ExerciseSheetsService,
    private courseService: CourseManagementService,
    private modalService: NgbModal,
    private lecturerAssignmentService: LecturerTaskAssignmentService
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
  }

  /**
   * Sets the course instance.
   *
   * @param value the value to set
   */
  public set courseInstance(value: IDisplayableCourseInstanceDTO) {
    this._courseInstance = value;

    forkJoin([this.loadExerciseSheetsPageAsync(), this.courseService.getExerciseSheetsOfCourseInstance(value.id)]).subscribe(
      ([, second]) => {
        this.selectedSheetsId = second.map(x => x.internalId);
        this._closedExerciseSheets = second.filter(x => x.closed).map(x => x.internalId);
      }
    );
  }

  /**
   * Returns the course instance.
   */
  public get courseInstance(): IDisplayableCourseInstanceDTO {
    return this._courseInstance!;
  }

  /**
   * Closes the currently open modal window.
   */
  public close(): void {
    this.activeModal.dismiss();
  }

  /**
   * Saves the form.
   */
  public save(): void {
    this.saveAsync();
  }

  /**
   * Returns the identity id from the given item.
   *
   * @param index the index
   * @param item the item
   */
  public trackIdentity(index: number, item: IExerciseSheetDisplayDTO): string {
    return item.internalId;
  }

  /**
   * Returns whether the given exercise sheet display dto is selected or not.
   *
   * @param item the exercise sheet display dto to check
   */
  public isSelected(item: IExerciseSheetDisplayDTO): boolean {
    return this.selectedSheetsId.includes(item.internalId);
  }

  /**
   * Returns whether the given exercise sheet is already closed or not.
   *
   * @param item the exercise sheet to check
   */
  public isClosed(item: IExerciseSheetDisplayDTO): boolean {
    return this._closedExerciseSheets.includes(item.internalId);
  }

  /**
   * Marks the given exercise sheet display dto as selected.
   *
   * @param item the item to mark
   */
  public markAsSelected(item: IExerciseSheetDisplayDTO): void {
    this.selectedSheetsId.push(item.internalId);
    this._selectedSheetIdsToSave.push(item.internalId);
  }

  /**
   * Opens the lecturer assignment overview for the given exercise sheet from the
   * currently selected course instance.
   *
   * @param item the selected exercise sheet
   */
  public openLecturerAssignmentOverview(item: IExerciseSheetDisplayDTO): void {
    const modalRef = this.modalService.open(LecturerTaskAssignmentOverviewComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as LecturerTaskAssignmentOverviewComponent).assignedSheetInfo = {
      courseInstanceId: this.courseInstance.id,
      exerciseSheetId: item.internalId,
    };
  }

  /**
   * Performs the page transition.
   */
  public transition(): void {
    this.loadExerciseSheetsPageAsync();
  }

  /**
   * Requests the overview of the achieved points for a specific exercise-sheet.
   *
   * @param item the exercise sheet
   */
  public exportPointsForExerciseSheet(item: IExerciseSheetDisplayDTO): void {
    const exerciseSheetUUID = item.internalId.substr(item.internalId.lastIndexOf('#') + 1);
    const courseInstanceUUID = this._courseInstance?.id.substr(this._courseInstance.id.lastIndexOf('#') + 1);
    this.lecturerAssignmentService.getExerciseSheetPointOverviewAsCSV(courseInstanceUUID!, exerciseSheetUUID);
  }

  /**
   * Opens or closes the requested exercise sheet.
   *
   * @param item the exercise sheet
   */
  public openOrCloseExerciseSheet(item: IExerciseSheetDisplayDTO): void {
    if (this.isClosed(item)) {
      this.openExerciseSheet(item);
    } else {
      this.closeExerciseSheet(item);
    }
  }

  /**
   * Closes the requested exercise sheet.
   *
   * @param item the exercise sheet
   */
  public closeExerciseSheet(item: IExerciseSheetDisplayDTO): void {
    this.lecturerAssignmentService.closeExerciseSheet(this._courseInstance!.id, item.internalId).subscribe(() => {
      item.closed = true;
      this._closedExerciseSheets.push(item.internalId);
    });
  }

  /**
   * Re-opens an already closed exercise sheet.
   *
   * @param item the exercise sheet
   */
  public openExerciseSheet(item: IExerciseSheetDisplayDTO): void {
    this.lecturerAssignmentService.openExerciseSheet(this._courseInstance!.id, item.internalId).subscribe(() => {
      item.closed = false;
      this._closedExerciseSheets.splice(this._closedExerciseSheets.indexOf(item.internalId));
    });
  }

  /**
   * Asynchronously saves the form.
   */
  private async saveAsync(): Promise<any> {
    await this.courseService.addExerciseSheetAssignment(this.courseInstance.id, this._selectedSheetIdsToSave).toPromise();
    this.activeModal.close();
  }

  /**
   * Asynchronously loads the exercise sheets page.
   */
  private async loadExerciseSheetsPageAsync(): Promise<any> {
    const response = await this.exerciseSheetService
      .getPagedExerciseSheetPage(
        {
          page: this.page - 1,
          size: this.itemsPerPage,
          sort: [],
        },
        undefined
      )
      .toPromise();

    this.totalItems = Number(response.headers.get(COUNT_HEADER));
    this.exerciseSheets = response.body ?? [];
  }
}
