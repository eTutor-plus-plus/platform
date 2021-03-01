import { Component, OnInit } from '@angular/core';
import { CourseManagementService } from '../../../course-management.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder } from '@angular/forms';
import { StudentService } from '../../../../shared/students/student-service';
import { IStudentFullNameInfoDTO } from '../../../../shared/students/students.model';
import { IDisplayableCourseInstanceDTO } from '../../../course-mangement.model';
import { JhiEventManager } from 'ng-jhipster';

/**
 * Component for managing the student assignment.
 */
@Component({
  selector: 'jhi-student-assignment-modal',
  templateUrl: './student-assignment-modal.component.html',
  styleUrls: ['./student-assignment-modal.component.scss'],
})
export class StudentAssignmentModalComponent implements OnInit {
  private _courseInstance?: IDisplayableCourseInstanceDTO;
  public isSaving = false;
  public assignmentForm = this.fb.group({
    students: [[], []],
    csvFile: [null, []],
  });

  public availableStudents: IStudentFullNameInfoDTO[] = [];

  /**
   * Constructor.
   *
   * @param courseService the injected course service
   * @param activeModal the injected active modal service
   * @param fb the injected form builder
   * @param studentService the injected student service
   * @param eventManager the injected jhi event manager
   */
  constructor(
    private courseService: CourseManagementService,
    private activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private studentService: StudentService,
    private eventManager: JhiEventManager
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
   * Asynchronously saves the assigned students.
   */
  private async saveAsync(): Promise<any> {
    this.isSaving = true;

    if (this.assignmentForm.get(['csvFile'])!.value !== null) {
      const fileList = this.assignmentForm.get(['csvFile'])!.value as FileList;
      if (fileList.length === 1) {
        const csvFile = fileList.item(0)!;

        try {
          await this.courseService.uploadStudents(this.courseInstance.id, csvFile).toPromise();
        } catch (e) {
          this.isSaving = false;
          throw e;
        }
        this.eventManager.broadcast('course-instance-stud-assignment-changed');
        this.isSaving = false;
        this.activeModal.close();
        return;
      }
    }

    try {
      await this.courseService.setAssignedStudents(this.courseInstance.id, this.assignmentForm.get(['students'])!.value).toPromise();
    } catch (e) {
      this.isSaving = false;
      throw e;
    }

    this.eventManager.broadcast('course-instance-stud-assignment-changed');
    this.isSaving = false;
    this.activeModal.close();
  }
}
