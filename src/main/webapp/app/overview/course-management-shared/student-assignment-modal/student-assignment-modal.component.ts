import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CourseManagementService } from '../../course-management/course-management.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder } from '@angular/forms';
import { StudentService } from '../../shared/students/student-service';
import { IStudentFullNameInfoDTO } from '../../shared/students/students.model';
import { IDisplayableCourseInstanceDTO } from '../../course-management/course-mangement.model';
import { NgxSpinnerService } from 'ngx-spinner';
import { EventManager } from 'app/core/util/event-manager.service';

/**
 * Component for managing the student assignment.
 */
@Component({
  selector: 'jhi-student-assignment-modal',
  templateUrl: './student-assignment-modal.component.html',
})
export class StudentAssignmentModalComponent implements OnInit {
  public isSaving = false;
  public fileSelected = false;
  public assignmentForm = this.fb.group({
    students: [[], []],
    csvFile: [null, []],
  });

  @ViewChild('csvFile', { static: true })
  public fileInputRef!: ElementRef;

  public availableStudents: IStudentFullNameInfoDTO[] = [];

  private _courseInstance?: IDisplayableCourseInstanceDTO;

  /**
   * Constructor.
   *
   * @param courseService the injected course service
   * @param activeModal the injected active modal service
   * @param fb the injected form builder
   * @param studentService the injected student service
   * @param eventManager the injected jhi event manager
   * @param spinner the injected spinner service
   */
  constructor(
    private courseService: CourseManagementService,
    private activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private studentService: StudentService,
    private eventManager: EventManager,
    private spinner: NgxSpinnerService
  ) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.studentService.getAvailableStudents().subscribe(value => (this.availableStudents = value));
  }

  /**
   * Saves the student assignment.
   */
  public save(): void {
    this.saveAsync();
  }

  /**
   * Closes the modal window
   */
  public close(): void {
    this.activeModal.dismiss();
  }

  /**
   * Sets the course instance.
   *
   * @param value the value to set
   */
  public set courseInstance(value: IDisplayableCourseInstanceDTO) {
    this._courseInstance = value;

    this.courseService.getAssignedStudentsOfCourseInstance(value.id).subscribe(assignedStudents =>
      this.assignmentForm.patchValue({
        students: assignedStudents.map(x => x.matriculationNumber),
      })
    );
  }

  /**
   * Returns the course instance.
   */
  public get courseInstance(): IDisplayableCourseInstanceDTO {
    return this._courseInstance!;
  }

  /**
   * Removes the file from the file input dialog
   */
  public removeFile(): void {
    this.assignmentForm.patchValue({
      csvFile: undefined,
    });
    this.fileSelected = false;
    this.fileInputRef.nativeElement.value = '';
  }

  /**
   * Detects a file fileupload
   */
  public fileChanged(): void {
    this.fileSelected = true;
  }

  /**
   * Asynchronously saves the assigned students.
   */
  private async saveAsync(): Promise<any> {
    this.isSaving = true;
    this.spinner.show();

    if (this.assignmentForm.get(['csvFile'])!.value !== undefined && this.assignmentForm.get(['csvFile'])!.value !== null) {
      const fileList = this.assignmentForm.get(['csvFile'])!.value as FileList;
      if (fileList.length === 1) {
        const csvFile = fileList.item(0)!;

        try {
          await this.courseService.uploadStudents(this.courseInstance.id, csvFile).toPromise();
        } catch (e) {
          this.spinner.hide();
          this.isSaving = false;
          throw e;
        }
        this.eventManager.broadcast('course-instance-stud-assignment-changed');
        this.spinner.hide();
        this.isSaving = false;
        this.activeModal.close();
        return;
      }
    }

    try {
      await this.courseService.setAssignedStudents(this.courseInstance.id, this.assignmentForm.get(['students'])!.value).toPromise();
    } catch (e) {
      this.spinner.hide();
      this.isSaving = false;
      throw e;
    }

    this.eventManager.broadcast('course-instance-stud-assignment-changed');
    this.spinner.hide();
    this.isSaving = false;
    this.activeModal.close();
  }
}
