import { Component, Input } from '@angular/core';
import { CourseManagementService } from '../course-management.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators } from '@angular/forms';
import { CourseModel } from '../course-mangement.model';
import { URL_OR_EMPTY_PATTERN } from 'app/config/input.constants';
import { EventManager } from 'app/core/util/event-manager.service';

/**
 * Component which is used to create / update a course.
 */
@Component({
  selector: 'jhi-update-course',
  templateUrl: './update-course.component.html',
})
export class UpdateCourseComponent {
  public updateForm = this.fb.group({
    name: ['', [Validators.required]],
    description: [''],
    type: [undefined, [Validators.required]],
    link: ['', [Validators.pattern(URL_OR_EMPTY_PATTERN)]],
  });

  public isSaving = false;

  private _course?: CourseModel;
  private _courseTypes: string[] = [];

  /**
   * Constructor.
   *
   * @param courseService the injected course service
   * @param activeModal the inject active modal service
   * @param fb the inject form builder
   * @param eventManager the inject jhi event manager
   */
  constructor(
    private courseService: CourseManagementService,
    private activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private eventManager: EventManager
  ) {}

  /**
   * Saves the form.
   */
  public save(): void {
    this.saveAsync();
  }

  /**
   * Closes the modal dialog.
   */
  public close(): void {
    this.activeModal.dismiss();
  }

  /**
   * Sets the course.
   *
   * @param value the course to set
   */
  public set course(value: CourseModel | undefined) {
    this._course = value;

    if (value) {
      let link = '';

      if (value.link) {
        link = value.link.toString();
      }

      this.updateForm.patchValue({
        name: value.name,
        description: value.description,
        type: value.courseType,
        link,
      });
    }
  }

  /**
   * Returns the course.
   */
  public get course(): CourseModel | undefined {
    return this._course;
  }

  /**
   * Sets the available course types.
   *
   * @param value the types to set
   */
  @Input()
  public set courseTypes(value: string[]) {
    if (value.length > 0) {
      this.updateForm.patchValue({
        type: value[0],
      });
    }
    this._courseTypes = value;
  }

  /**
   * Returns the available course types.
   */
  public get courseTypes(): string[] {
    return this._courseTypes;
  }

  /**
   * Returns whether the component is in edit mode
   * or not which means new mode.
   */
  public get isEditMode(): boolean {
    return this.course !== undefined;
  }

  /**
   * Saves the form asynchronously.
   */
  private async saveAsync(): Promise<any> {
    const courseToSave: CourseModel = {
      name: this.updateForm.get(['name'])!.value,
      description: this.updateForm.get(['description'])!.value,
      courseType: this.updateForm.get(['type'])!.value,
      instanceCount: 0,
    };

    if (this.updateForm.get(['link'])!.value) {
      courseToSave.link = new URL(this.updateForm.get(['link'])!.value);
    }

    if (this.course) {
      // Create new course
      courseToSave.creator = this.course.creator;
      courseToSave.id = this.course.id;

      await this.courseService.putCourse(courseToSave).toPromise();
    } else {
      // Update existing course
      await this.courseService.postCourse(courseToSave).toPromise();
    }
    this.eventManager.broadcast('courseChanged');
    this.close();
  }
}
