/**
 * Module for the dispatcher component.
 */
import { SharedModule } from 'app/shared/shared.module';
import { RouterModule } from '@angular/router';
import { dispatcherRoute } from 'app/dispatcher/dispatcher.route';
import { NgModule } from '@angular/core';

import { DispatcherComponent } from 'app/dispatcher/dispatcher.component';
import { AssignmentComponent } from 'app/dispatcher/assignment/assignment.component';
import { AssignmentsComponent } from 'app/dispatcher/assignments/assignments.component';

@NgModule({
  imports: [SharedModule, RouterModule.forChild(dispatcherRoute)],
  declarations: [DispatcherComponent, AssignmentComponent, AssignmentsComponent],
})
export class DispatcherModule {}
