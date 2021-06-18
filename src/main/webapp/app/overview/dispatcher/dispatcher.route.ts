import { Routes } from '@angular/router';
// import { Authority } from 'app/config/authority.constants';
import { DispatcherComponent } from 'app/overview/dispatcher/dispatcher.component';
import { AssignmentsComponent } from 'app/overview/dispatcher/assignments/assignments.component';

/**
 * Dispatcher related routes.
 */
export const dispatcherRoute: Routes = [
  {
    path: '',
    component: DispatcherComponent,
  },
  {
    path: 'assignments',
    component: AssignmentsComponent,
  },
];
