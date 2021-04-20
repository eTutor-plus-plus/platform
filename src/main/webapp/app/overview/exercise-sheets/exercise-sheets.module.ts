import { NgModule } from '@angular/core';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { ExerciseSheetsOverviewComponent } from './exercise-sheets-overview/exercise-sheets-overview.component';
import { exerciseSheetRoutes } from './exercise-sheets.route';
import { ExerciseSheetUpdateComponent } from './exercise-sheet-update/exercise-sheet-update.component';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';
import { SharedModule } from 'app/shared/shared.module';

/**
 * Module for exercise related modules.
 */
@NgModule({
  imports: [
    SharedModule,
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
