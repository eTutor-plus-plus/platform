import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { TasksOverviewComponent } from './tasks-overview/tasks-overview.component';
import { tasksRoutes } from './tasks.route';
import { TaskUpdateComponent } from './tasks-overview/task-update/task-update.component';
import { TaskAssignmentUpdateComponent } from './tasks-overview/task-assignment-update/task-assignment-update.component';
import { TaskDisplayComponent } from './tasks-overview/task-display/task-display.component';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';

/**
 * Module for task related components.
 */
@NgModule({
  imports: [
    SharedModule,
    OverviewSharedModule,
    RouterModule.forChild(tasksRoutes),
    ConfirmationPopoverModule.forRoot({
      confirmButtonType: 'danger',
      closeOnOutsideClick: true,
      appendToBody: true,
    }),
  ],
  declarations: [TasksOverviewComponent, TaskUpdateComponent, TaskAssignmentUpdateComponent, TaskDisplayComponent],
})
export class TasksModule {}
