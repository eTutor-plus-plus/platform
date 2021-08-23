import { Routes } from '@angular/router';
import { SqlTableComponent } from './sql-table/sql-table.component';

/**
 * Tasks related routes
 */
export const sqlTablesRoute: Routes = [
  {
    path: '',
    component: SqlTableComponent,
  },
];
