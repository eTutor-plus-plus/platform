import { Component } from '@angular/core';
import { TaskSubmissionsModel } from '../task-submissions/task-submissions.model';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

/**
 * Modal window component for testing submissions
 */

@Component({
  selector: 'jhi-lecturer-run-submission',
  templateUrl: './lecturer-run-submission.component.html',
  styleUrls: ['./lecturer-run-submission.component.scss'],
})
export class LecturerRunSubmissionComponent {
  /**
   * Wraps information about the submission to be evaluated
   */
  public submissionEntry!: TaskSubmissionsModel;
  public matriculationNo: string | undefined = '';
  public showHeader = true;

  constructor(private activeModal: NgbActiveModal, private modalService: NgbModal) {}

  /**
   * Closes the modal window.
   */
  public close(): void {
    this.activeModal.close();
  }
}
