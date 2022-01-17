import { Routes } from '@angular/router';
import { SqlTableComponent } from './sql-table/sql-table.component';

/**
 *  Routes related to the {@link SqlTablesModule}
 */
export const sqlTablesRoute: Routes = [
  {
    path: ':tableName',
    component: SqlTableComponent,
  },
];
