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
  public submissions: TaskSubmissionsModel[] = [];
  public submittedSubmissions: TaskSubmissionsModel[] = [];
  public exerciseSheetUUID: string | undefined = '';
  public orderNo: string | undefined = '';
  public courseInstanceUUID: string | undefined = '';
  public matriculationNo: string | undefined = '';

  constructor(private activeModal: NgbActiveModal, private modalService: NgbModal, private studentService: StudentService) {}

  /**
   * Closes the modal window.
   */
  public close(): void {
    this.activeModal.close();
  }

  ngOnInit(): void {
    this.submittedSubmissions = this.submissions.filter(x => x.isSubmitted);
  }

  openEditor(entry: TaskSubmissionsModel): void {
    const modalRef = this.modalService.open(LecturerRunSubmissionComponent, { backdrop: 'static', size: 'xl' });
    (modalRef.componentInstance as LecturerRunSubmissionComponent).submissionEntry = entry;
  }
}
