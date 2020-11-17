import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CourseModel } from '../course-mangement.model';

/**
 * Component which is used to display a course.
 */
@Component({
  selector: 'jhi-view-course',
  templateUrl: './view-course.component.html',
})
export class ViewCourseComponent implements OnInit {
  public course!: CourseModel;

  /**
   * Constructor.
   *
   * @param activeModal the injected active modal service
   */
  constructor(private activeModal: NgbActiveModal) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}

  /**
   * Closes the current modal window.
   */
  public close(): void {
    this.activeModal.dismiss();
  }
}
