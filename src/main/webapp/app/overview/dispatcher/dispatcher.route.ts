import { Routes } from '@angular/router';
// import { Authority } from 'app/config/authority.constants';
import { DispatcherComponent } from 'app/overview/dispatcher/dispatcher.component';

/**
 * Dispatcher related routes.
 */
export const dispatcherRoute: Routes = [
  {
    path: '',
    component: DispatcherComponent,
  },
];
