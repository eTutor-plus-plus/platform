import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedModule } from '../shared/shared.module';
import { RouterModule } from '@angular/router';
import { overviewRoute } from './overview.route';
import { OverviewComponent } from './overview.component';
/**
 * Module for the overview component.
 */
@NgModule({
  imports: [EtutorPlusPlusSharedModule, RouterModule.forChild(overviewRoute)],
  declarations: [OverviewComponent],
})
export class OverviewModule {}
