import { Routes } from '@angular/router';
import { CourseManagementComponent } from './course-management.component';
import { CourseInstanceOverviewComponent } from './course-instances/course-instance-overview/course-instance-overview.component';

/**
 * Course management related routes.
 */
export const courseManagementRoutes: Routes = [
  {
    path: '',
    component: CourseManagementComponent,
  },
  {
    path: 'course-instances/of/:courseName',
    component: CourseInstanceOverviewComponent,
  },
];
