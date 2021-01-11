import { Routes } from '@angular/router';
import { TasksOverviewComponent } from './tasks-overview/tasks-overview.component';

/**
 * Tasks related routes
 */
export const tasksRoutes: Routes = [
  {
    path: '',
    component: TasksOverviewComponent,
  },
];
