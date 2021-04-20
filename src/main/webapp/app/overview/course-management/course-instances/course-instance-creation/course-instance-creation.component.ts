import { Component } from '@angular/core';
import { CourseManagementService } from '../../course-management.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators } from '@angular/forms';
import { ICourseModel, INewCourseInstanceDTO, Term } from '../../course-mangement.model';
import { getYear } from 'date-fns';

/**
 * Component which displays the course instance creation window.
 */
@Component({
  selector: 'jhi-course-instance-creation',
  templateUrl: './course-instance-creation.component.html',
  styleUrls: ['./course-instance-creation.component.scss'],
})
export class CourseInstanceCreationComponent {
  public isSaving = false;
  public readonly terms = Term.Values;
  public readonly currentYear = getYear(new Date());

  public instanceForm = this.fb.group({
    year: [this.currentYear, [Validators.required, Validators.min(this.currentYear)]],
    termId: [this.terms[0], [Validators.required]],
    description: [''],
  });

  private _course?: ICourseModel;

  /**
   * Constructor.
   *
   * @param courseService the injected course management service
   * @param activeModal the injected active modal service
   * @param fb the injected form builder
   */
  constructor(private courseService: CourseManagementService, private activeModal: NgbActiveModal, private fb: FormBuilder) {}

  /**
   * Saves the course instance.
   */
  public save(): void {
    this.saveAsync();
  }

  /**
   * Dismisses the window.
   */
  public cancel(): void {
    this.activeModal.dismiss();
  }

  /**
   * Sets the course.
   *
   * @param value the course value to set
   */
  public set course(value: ICourseModel) {
    this._course = value;
  }

  /**
   * Returns the underlying course object.
   */
  public get course(): ICourseModel {
    return this._course!;
  }

  /**
   * Asynchronously saves the newly created course instance.
   */
  private async saveAsync(): Promise<any> {
    this.isSaving = true;

    let description = this.instanceForm.get(['description'])!.value as string;
    const termId = (this.instanceForm.get(['termId'])!.value as Term).value;

    const newInstance: INewCourseInstanceDTO = {
      year: this.instanceForm.get(['year'])!.value,
      termId,
      courseId: this.course.id!,
    };

    if (description && (description = description.trim())) {
      newInstance.description = description;
    }

    try {
      await this.courseService.createInstance(newInstance).toPromise();
    } catch (e) {
      this.isSaving = false;
      throw e;
    }
    this.isSaving = false;
    this.activeModal.close();
  }
}
