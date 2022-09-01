import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { learningGoalsRoute } from './learning-goals.route';
import { RouterModule } from '@angular/router';
import { LearningGoalsComponent } from './learning-goals.component';
import { LearningGoalDisplayComponent } from './learning-goal-display/learning-goal-display.component';
import { LearningGoalCreationComponent } from './learning-goal-creation/learning-goal-creation.component';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { DependencyManagerWindowComponent } from './dependency-manager-window/dependency-manager-window.component';
import { SupergoalManagerWindowComponent } from './supergoal-manager-window/supergoal-manager-window.component';
import { LearningGoalDescriptionModalComponent } from './learning-goal-description-modal/learning-goal-description-modal.component';

/**
 * Module for the learning goal related components.
 */
@NgModule({
  imports: [SharedModule, OverviewSharedModule, RouterModule.forChild(learningGoalsRoute)],
  declarations: [
    LearningGoalsComponent,
    LearningGoalDisplayComponent,
    LearningGoalCreationComponent,
    DependencyManagerWindowComponent,
    SupergoalManagerWindowComponent,
    LearningGoalDescriptionModalComponent,
  ],
})
export class LearningGoalsModule {}
