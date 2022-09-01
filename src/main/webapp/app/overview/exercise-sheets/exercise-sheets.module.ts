import { NgModule } from '@angular/core';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { ExerciseSheetsOverviewComponent } from './exercise-sheets-overview/exercise-sheets-overview.component';
import { exerciseSheetRoutes } from './exercise-sheets.route';
import { ExerciseSheetUpdateComponent } from './exercise-sheet-update/exercise-sheet-update.component';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';
import { SharedModule } from 'app/shared/shared.module';
import { ExerciseSheetContextMenuComponent } from './exercise-sheet-context-menu/exercise-sheet-context-menu.component';

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
  declarations: [ExerciseSheetsOverviewComponent, ExerciseSheetUpdateComponent, ExerciseSheetContextMenuComponent],
})
export class ExerciseSheetsModule {}
