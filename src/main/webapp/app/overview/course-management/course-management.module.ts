import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../../shared/shared.module';
import { RouterModule } from '@angular/router';
import { courseManagementRoutes } from './course-management.route';
import { CourseManagementComponent } from './course-management.component';

/**
 * The module for course management related components.
 */
@NgModule({
  imports: [EtutorPlusPlusSharedModule, RouterModule.forChild(courseManagementRoutes)],
  declarations: [CourseManagementComponent],
})
export class CourseManagementModule {}
