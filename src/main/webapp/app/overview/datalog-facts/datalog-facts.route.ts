import { Routes } from '@angular/router';
import { DatalogFactsComponent } from './datalog-facts.component';

/**
 * Routes for the datalag-facts module
 */
export const datalogFactsRoute: Routes = [
  {
    path: ':id',
    component: DatalogFactsComponent,
  },
];
