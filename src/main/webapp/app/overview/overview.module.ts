import { NgModule } from '@angular/core';
import { SharedModule } from '../shared/shared.module';
import { RouterModule } from '@angular/router';
import { overviewRoute } from './overview.route';
import { OverviewComponent } from './overview.component';
import { StudentOverviewComponent } from './student-overview/student-overview.component';
import { CourseTaskOverviewComponent } from './student-overview/course-task-overview/course-task-overview.component';
import { StudentSelfEvaluationComponent } from './student-overview/student-self-evaluation/student-self-evaluation.component';
import { StudentExerciseSheetTasksComponent } from './student-overview/course-task-overview/student-exercise-sheet-tasks/student-exercise-sheet-tasks.component';
import { StudentTaskComponent } from './student-overview/course-task-overview/student-task/student-task.component';
import { OverviewSharedModule } from 'app/overview/shared/overview-shared.module';
import { DispatcherModule } from 'app/overview/dispatcher/dispatcher.module';
import { LecturerOverviewComponent } from './lecturer-overview/lecturer-overview.component';
import { CourseManagementSharedModule } from './course-management-shared/course-management-shared.module';
import { CourseInstanceStatisticsOverviewComponent } from './lecturer-overview/course-instance-statistics-overview/course-instance-statistics-overview.component';
import { NgxSpinnerModule } from 'ngx-spinner';
import { StudentExerciseSheetGoalsComponent } from './student-overview/course-task-overview/student-exercise-sheet-tasks/student-exercise-sheet-goals/student-exercise-sheet-goals.component';

/**
 * Module for the overview component.
 */
@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(overviewRoute),
    OverviewSharedModule,
    DispatcherModule,
    CourseManagementSharedModule,
    NgxSpinnerModule,
  ],
  declarations: [
    OverviewComponent,
    StudentOverviewComponent,
    CourseTaskOverviewComponent,
    StudentSelfEvaluationComponent,
    StudentExerciseSheetTasksComponent,
    StudentTaskComponent,
    LecturerOverviewComponent,
    CourseInstanceStatisticsOverviewComponent,
    StudentExerciseSheetGoalsComponent,
  ],
})
export class OverviewModule {}
