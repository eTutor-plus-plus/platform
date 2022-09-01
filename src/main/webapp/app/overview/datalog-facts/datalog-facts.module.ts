import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { DatalogFactsComponent } from './datalog-facts.component';
import { datalogFactsRoute } from './datalog-facts.route';

/**
 * Module for displaying datalog facts
 */
@NgModule({
  imports: [SharedModule, OverviewSharedModule, RouterModule.forChild(datalogFactsRoute)],
  declarations: [DatalogFactsComponent],
})
export class DatalogFactsModule {}
