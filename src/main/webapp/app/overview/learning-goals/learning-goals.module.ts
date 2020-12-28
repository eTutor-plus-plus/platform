import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../../shared/shared.module';
import { learningGoalsRoute } from './learning-goals.route';
import { RouterModule } from '@angular/router';
import { LearningGoalsComponent } from './learning-goals.component';
import { LearningGoalDisplayComponent } from './learning-goal-display/learning-goal-display.component';
import { LearningGoalCreationComponent } from './learning-goal-creation/learning-goal-creation.component';
import { LearningGoalTreeviewComponent } from './learning-goal-treeview/learning-goal-treeview.component';

/**
 * Module for the learning goal related components.
 */
@NgModule({
  imports: [EtutorPlusPlusSharedModule, RouterModule.forChild(learningGoalsRoute)],
  declarations: [LearningGoalsComponent, LearningGoalDisplayComponent, LearningGoalCreationComponent, LearningGoalTreeviewComponent],
  exports: [LearningGoalTreeviewComponent],
})
export class LearningGoalsModule {}
