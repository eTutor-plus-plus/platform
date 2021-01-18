import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../../shared/shared.module';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { ExerciseSheetsOverviewComponent } from './exercise-sheets-overview/exercise-sheets-overview.component';
import { exerciseSheetRoutes } from './exercise-sheets.route';

/**
 * Module for exercise related modules.
 */
@NgModule({
  imports: [EtutorPlusPlusSharedModule, OverviewSharedModule, RouterModule.forChild(exerciseSheetRoutes)],
  declarations: [ExerciseSheetsOverviewComponent],
})
export class ExerciseSheetsModule {}
