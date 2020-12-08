import { Routes } from '@angular/router';
import { LearningGoalsComponent } from './learning-goals.component';

/**
 * Learning goal related routes.
 */
export const learningGoalsRoute: Routes = [
  {
    path: '',
    component: LearningGoalsComponent,
  },
];
