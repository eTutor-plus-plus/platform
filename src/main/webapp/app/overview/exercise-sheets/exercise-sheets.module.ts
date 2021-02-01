import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../../shared/shared.module';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { ExerciseSheetsOverviewComponent } from './exercise-sheets-overview/exercise-sheets-overview.component';
import { exerciseSheetRoutes } from './exercise-sheets.route';
import { ExerciseSheetUpdateComponent } from './exercise-sheet-update/exercise-sheet-update.component';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';

/**
 * Module for exercise related modules.
 */
@NgModule({
  imports: [
    EtutorPlusPlusSharedModule,
    OverviewSharedModule,
    RouterModule.forChild(exerciseSheetRoutes),
    ConfirmationPopoverModule.forRoot({
      confirmButtonType: 'danger',
      closeOnOutsideClick: true,
      appendToBody: true,
    }),
  ],
  declarations: [ExerciseSheetsOverviewComponent, ExerciseSheetUpdateComponent],
})
export class ExerciseSheetsModule {}
