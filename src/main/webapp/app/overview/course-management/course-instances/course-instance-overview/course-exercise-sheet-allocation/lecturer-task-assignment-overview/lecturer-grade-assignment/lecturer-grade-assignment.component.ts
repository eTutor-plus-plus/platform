import { Component, OnInit } from '@angular/core';
import { LecturerTaskAssignmentService } from '../lecturer-task-assignment.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

/**
 * Modal component for displaying
 */
@Component({
  selector: 'jhi-lecturer-grade-assignment',
  templateUrl: './lecturer-grade-assignment.component.html',
  styleUrls: ['./lecturer-grade-assignment.component.scss'],
})
export class LecturerGradeAssignmentComponent implements OnInit {
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
   * Closes the modal window.
   */
  public close(): void {
    this.activeModal.close();
  }
}
