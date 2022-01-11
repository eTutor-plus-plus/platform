import { Routes } from '@angular/router';
import { DatalogFactsComponent } from './datalog-facts.component';

/**
 * Tasks related routes
 */
export const datalogFactsRoute: Routes = [
  {
    path: ':id',
    component: DatalogFactsComponent,
  },
];
