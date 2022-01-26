import { Routes } from '@angular/router';
import { OverviewComponent } from './overview.component';
import { CourseTaskOverviewComponent } from './student-overview/course-task-overview/course-task-overview.component';
import { StudentSelfEvaluationComponent } from './student-overview/student-self-evaluation/student-self-evaluation.component';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { Authority } from 'app/config/authority.constants';
import { StudentExerciseSheetTasksComponent } from './student-overview/course-task-overview/student-exercise-sheet-tasks/student-exercise-sheet-tasks.component';
import { StudentTaskComponent } from 'app/overview/student-overview/course-task-overview/student-task/student-task.component';
import { CourseInstanceStatisticsOverviewComponent } from './lecturer-overview/course-instance-statistics-overview/course-instance-statistics-overview.component';

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
  {
    path: 'student/exercises/:exerciseSheetUUID/tasks',
    component: StudentExerciseSheetTasksComponent,
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.STUDENT],
    },
  },
  {
    path: 'student/exercises/:exerciseSheetUUID/tasks/task/:taskUUID/taskNo/:taskNo',
    component: StudentTaskComponent,
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.STUDENT],
    },
  },
  {
    path: 'sql-tables',
    data: {
      requireLogin: true,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./sql-tables/sql-tables.module').then(m => m.SqlTablesModule),
  },
  {
    path: 'datalog-facts',
    data: {
      requireLogin: true,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./datalog-facts/datalog-facts.module').then(m => m.DatalogFactsModule),
  },

  {
    path: 'XML',
    data: {
      requireLogin: true,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./xml-files/xml-files.module').then(m => m.XmlFilesModule),
  },
  {
    path: 'student/self-assessment',
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.STUDENT],
    },
    component: StudentSelfEvaluationComponent,
  },
  {
    path: 'lecturer/statistics/:instanceId',
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.INSTRUCTOR],
    },
    component: CourseInstanceStatisticsOverviewComponent,
  },
];
