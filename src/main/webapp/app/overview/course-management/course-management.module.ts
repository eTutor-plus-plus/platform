import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../../shared/shared.module';
import { RouterModule } from '@angular/router';
import { courseManagementRoutes } from './course-management.route';
import { CourseManagementComponent } from './course-management.component';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';
import { UpdateCourseComponent } from './update-course/update-course.component';
import { ViewCourseComponent } from './view-course/view-course.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { LearningGoalAssignmentUpdateComponent } from './learning-goal-assignment-update/learning-goal-assignment-update.component';
import { LearningGoalAssignmentDisplayComponent } from './learning-goal-assignment-display/learning-goal-assignment-display.component';
import { OverviewSharedModule } from '../shared/overview-shared.module';

/**
 * The module for course management related components.
 */
@NgModule({
  imports: [
    EtutorPlusPlusSharedModule,
    OverviewSharedModule,
    RouterModule.forChild(courseManagementRoutes),
    ConfirmationPopoverModule.forRoot({
      confirmButtonType: 'danger',
      closeOnOutsideClick: true,
      appendToBody: true,
    }),
    NgSelectModule,
  ],
  declarations: [
    CourseManagementComponent,
    UpdateCourseComponent,
    ViewCourseComponent,
    LearningGoalAssignmentUpdateComponent,
    LearningGoalAssignmentDisplayComponent,
  ],
})
export class CourseManagementModule {}