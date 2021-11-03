import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { SqlTableComponent } from './sql-table/sql-table.component';
import { sqlTablesRoute } from './sql-tables.route';

/**
 * Module for displaying sql-tables
 */
@NgModule({
  imports: [SharedModule, OverviewSharedModule, RouterModule.forChild(sqlTablesRoute)],
  declarations: [SqlTableComponent],
})
export class SqlTablesModule {}
