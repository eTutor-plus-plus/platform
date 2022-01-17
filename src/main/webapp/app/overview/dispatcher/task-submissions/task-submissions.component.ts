import { Component, OnInit } from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { StudentService } from '../../shared/students/student-service';
import { TaskSubmissionsModel } from './task-submissions.model';
import { LecturerRunSubmissionComponent } from '../lecturer-run-submission/lecturer-run-submission.component';

/**
 * Modal window component for displaying submissions made by a student for an individual task
 */

@Component({
  selector: 'jhi-task-submissions',
  templateUrl: './task-submissions.component.html',
})
export class TaskSubmissionsComponent implements OnInit {
  /**
   * The submissions that are displayed
   */
  public submissions: TaskSubmissionsModel[] = [];
  /**
   * A filtered list that stores all submissions from {@link submissions} that have been submitted
   */
  public submittedSubmissions: TaskSubmissionsModel[] = [];
  /**
   * Identifies the exercise sheet
   */
  public exerciseSheetUUID: string | undefined = '';
  /**
   * The order number of the task
   */
  public orderNo: string | undefined = '';
  /**
   * The course instance
   */
  public courseInstanceUUID: string | undefined = '';
  /**
   * The matriculation number
   */
  public matriculationNo: string | undefined = '';

  constructor(private activeModal: NgbActiveModal, private modalService: NgbModal, private studentService: StudentService) {}

  /**
   * Closes the modal window.
   */
  public close(): void {
    this.activeModal.close();
  }

  /**
   * Lifecycle method that filters the submissions
   */
  ngOnInit(): void {
    this.submittedSubmissions = this.submissions.filter(x => x.isSubmitted);
  }

  /**
   * Opens a {@link LecturerRunSubmissionComponent} with attributes from a given submission {@see TaskSubmissionsModel}
   * @param entry
   */
  openEditor(entry: TaskSubmissionsModel): void {
    const modalRef = this.modalService.open(LecturerRunSubmissionComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as LecturerRunSubmissionComponent).submissionEntry = entry;
    (modalRef.componentInstance as LecturerRunSubmissionComponent).matriculationNo = this.matriculationNo!;
  }
}
