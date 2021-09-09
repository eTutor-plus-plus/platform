import { Component } from '@angular/core';
import { TaskSubmissionsModel } from '../task-submissions/task-submissions.model';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

/**
 * Modal window component for displaying submissions made by a student for an individual task
 */

@Component({
  selector: 'jhi-lecturer-run-submission',
  templateUrl: './lecturer-run-submission.component.html',
})
export class LecturerRunSubmissionComponent {
  public submissionEntry!: TaskSubmissionsModel;

  constructor(private activeModal: NgbActiveModal, private modalService: NgbModal) {}

  /**
   * Closes the modal window.
   */
  public close(): void {
    this.activeModal.close();
  }
}
