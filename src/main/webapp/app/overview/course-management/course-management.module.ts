import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../../shared/shared.module';
import { RouterModule } from '@angular/router';
import { courseManagementRoutes } from './course-management.route';
import { CourseManagementComponent } from './course-management.component';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';
import { UpdateCourseComponent } from './update-course/update-course.component';
import { QuillModule } from 'ngx-quill';

/**
 * The module for course management related components.
 */
@NgModule({
  imports: [
    EtutorPlusPlusSharedModule,
    RouterModule.forChild(courseManagementRoutes),
    ConfirmationPopoverModule.forRoot({
      confirmButtonType: 'danger',
      closeOnOutsideClick: true,
      appendToBody: true,
    }),
    QuillModule,
  ],
  declarations: [CourseManagementComponent, UpdateCourseComponent],
})
export class CourseManagementModule {}
