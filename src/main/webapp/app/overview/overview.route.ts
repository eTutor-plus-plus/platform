import { Routes } from '@angular/router';
import { OverviewComponent } from './overview.component';
import { UserRouteAccessService } from '../core/auth/user-route-access-service';
import { Authority } from '../shared/constants/authority.constants';
import { CourseTaskOverviewComponent } from './student-overview/course-task-overview/course-task-overview.component';

/**
 * Overview related routes.
 */
export const overviewRoute: Routes = [
  {
    path: '',
    component: OverviewComponent,
  },
  {
    path: 'learning-goals',
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.INSTRUCTOR],
    },
    loadChildren: () => import('./learning-goals/learning-goals.module').then(m => m.LearningGoalsModule),
  },
  {
    path: 'courses',
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.INSTRUCTOR],
    },
    loadChildren: () => import('./course-management/course-management.module').then(m => m.CourseManagementModule),
  },
  {
    path: 'tasks',
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.INSTRUCTOR],
    },
    loadChildren: () => import('./tasks/tasks.module').then(m => m.TasksModule),
  },
  {
    path: 'exercise-sheets',
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.INSTRUCTOR],
    },
    loadChildren: () => import('./exercise-sheets/exercise-sheets.module').then(m => m.ExerciseSheetsModule),
  },
  {
    path: 'student/exercises',
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.STUDENT],
    },
    component: CourseTaskOverviewComponent,
  },
];
