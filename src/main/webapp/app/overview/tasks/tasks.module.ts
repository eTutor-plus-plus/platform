import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { TasksOverviewComponent } from './tasks-overview/tasks-overview.component';
import { tasksRoutes } from './tasks.route';
import { TaskUpdateComponent } from './tasks-overview/task-update/task-update.component';
import { TaskAssignmentUpdateComponent } from './tasks-overview/task-assignment-update/task-assignment-update.component';
import { TaskDisplayComponent } from './tasks-overview/task-display/task-display.component';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';
import { TaskGroupManagementComponent } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.component';
import { TaskGroupUpdateComponent } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-update/task-group-update.component';
import { monacoConfig } from '../dispatcher/monaco-config';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';

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
    MonacoEditorModule.forRoot(monacoConfig),
  ],
  declarations: [
    TasksOverviewComponent,
    TaskUpdateComponent,
    TaskAssignmentUpdateComponent,
    TaskDisplayComponent,
    TaskGroupManagementComponent,
    TaskGroupUpdateComponent,
  ],
})
export class TasksModule {}
