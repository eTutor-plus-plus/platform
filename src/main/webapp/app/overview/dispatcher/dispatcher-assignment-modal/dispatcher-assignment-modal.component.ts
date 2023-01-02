import { Component } from '@angular/core';
import { TaskSubmissionsModel } from '../task-submissions/task-submissions.model';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

/**
 * Modal window component for testing submissions
 */

@Component({
  selector: 'jhi-lecturer-run-submission',
  templateUrl: './dispatcher-assignment-modal.component.html',
  styleUrls: ['./dispatcher-assignment-modal.component.scss'],
})
export class DispatcherAssignmentModalComponent {
  /**
   * Wraps information about the submission to be evaluated
   */
  public submissionEntry!: TaskSubmissionsModel;
  public matriculationNo: string | undefined = '';
  public showHeader = true;
  public showSubmitButton = false;

  constructor(private activeModal: NgbActiveModal, private modalService: NgbModal) {}

  /**
   * Closes the modal window.
   */
  public close(): void {
    this.activeModal.close();
  }
}
