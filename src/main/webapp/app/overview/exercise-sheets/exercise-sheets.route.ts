import { Routes } from '@angular/router';
import { ExerciseSheetsOverviewComponent } from './exercise-sheets-overview/exercise-sheets-overview.component';

/**
 * Exercise sheet related routes.
 */
export const exerciseSheetRoutes: Routes = [
  {
    path: '',
    component: ExerciseSheetsOverviewComponent,
  },
];
