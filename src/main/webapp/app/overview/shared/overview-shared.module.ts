import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../../shared/shared.module';
import { LearningGoalTreeviewComponent } from './learning-goal-treeview/learning-goal-treeview.component';

/**
 * Module for shared overview components.
 */
@NgModule({
  imports: [EtutorPlusPlusSharedModule],
  declarations: [LearningGoalTreeviewComponent],
  exports: [LearningGoalTreeviewComponent],
})
export class OverviewSharedModule {}
