import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../shared/shared.module';
import { RouterModule } from '@angular/router';
import { overviewRoute } from './overview.route';
import { OverviewComponent } from './overview.component';
import { StudentOverviewComponent } from './student-overview/student-overview.component';
import { CourseTaskOverviewComponent } from './student-overview/course-task-overview/course-task-overview.component';
import { StudentSelfEvaluationComponent } from './student-overview/student-self-evaluation/student-self-evaluation.component';

/**
 * Module for the overview component.
 */
@NgModule({
  imports: [EtutorPlusPlusSharedModule, RouterModule.forChild(overviewRoute)],
  declarations: [OverviewComponent, StudentOverviewComponent, CourseTaskOverviewComponent, StudentSelfEvaluationComponent],
})
export class OverviewModule {}
