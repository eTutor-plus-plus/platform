import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../../shared/shared.module';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { TasksOverviewComponent } from './tasks-overview/tasks-overview.component';
import { tasksRoutes } from './tasks.route';

/**
 * Module for task related components.
 */
@NgModule({
  imports: [EtutorPlusPlusSharedModule, OverviewSharedModule, RouterModule.forChild(tasksRoutes)],
  declarations: [TasksOverviewComponent],
})
export class TasksModule {}
