import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { StudentAssignmentModalComponent } from './student-assignment-modal/student-assignment-modal.component';
import { CourseExerciseSheetAllocationComponent } from './course-exercise-sheet-allocation/course-exercise-sheet-allocation.component';
import { LecturerTaskAssignmentOverviewComponent } from './course-exercise-sheet-allocation/lecturer-task-assignment-overview/lecturer-task-assignment-overview.component';
import { LecturerGradeAssignmentComponent } from './course-exercise-sheet-allocation/lecturer-task-assignment-overview/lecturer-grade-assignment/lecturer-grade-assignment.component';
import { NgxSpinnerModule } from 'ngx-spinner';
import { NgSelectModule } from '@ng-select/ng-select';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { TaskSubmissionsComponent } from '../dispatcher/task-submissions/task-submissions.component';

/**
 * Module for shared course management components.
 */
@NgModule({
  imports: [SharedModule, NgxSpinnerModule, NgSelectModule, OverviewSharedModule],
  declarations: [
    StudentAssignmentModalComponent,
    CourseExerciseSheetAllocationComponent,
    LecturerTaskAssignmentOverviewComponent,
    LecturerGradeAssignmentComponent,
    TaskSubmissionsComponent,
  ],
  exports: [
    StudentAssignmentModalComponent,
    CourseExerciseSheetAllocationComponent,
    LecturerTaskAssignmentOverviewComponent,
    LecturerGradeAssignmentComponent,
  ],
})
export class CourseManagementSharedModule {}
