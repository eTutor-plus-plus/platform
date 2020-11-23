import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../../shared/shared.module';
import { RouterModule } from '@angular/router';
import { courseManagementRoutes } from './course-management.route';
import { CourseManagementComponent } from './course-management.component';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';
import { UpdateCourseComponent } from './update-course/update-course.component';
import { ViewCourseComponent } from './view-course/view-course.component';
import { NgSelectModule } from '@ng-select/ng-select';

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
    NgSelectModule,
  ],
  declarations: [CourseManagementComponent, UpdateCourseComponent, ViewCourseComponent],
})
export class CourseManagementModule {}
