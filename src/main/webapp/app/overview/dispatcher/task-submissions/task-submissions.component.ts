import { Component } from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { StudentService } from '../../shared/students/student-service';
import { TaskSubmissionsModel } from './task-submissions.model';

/**
 * Modal window component for displaying submissions made by a student for an individual task
 */

@Component({
  selector: 'jhi-task-submissions',
  templateUrl: './task-submissions.component.html',
})
export class TaskSubmissionsComponent {
  public submissions: TaskSubmissionsModel[] = [];
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
}
