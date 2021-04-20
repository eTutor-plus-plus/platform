import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { LearningGoalTreeviewComponent } from './learning-goal-treeview/learning-goal-treeview.component';

/**
 * Module for shared overview components.
 */
@NgModule({
  imports: [SharedModule],
  declarations: [LearningGoalTreeviewComponent],
  exports: [LearningGoalTreeviewComponent],
})
export class OverviewSharedModule {}
