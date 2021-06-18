/**
 * Module for the dispatcher component.
 */
import { SharedModule } from 'app/shared/shared.module';
import { RouterModule } from '@angular/router';
import { dispatcherRoute } from 'app/overview/dispatcher/dispatcher.route';
import { NgModule } from '@angular/core';
import { MonacoEditorModule } from 'ngx-monaco-editor';

import { DispatcherComponent } from 'app/overview/dispatcher/dispatcher.component';
import { AssignmentComponent } from 'app/overview/dispatcher/assignment/assignment.component';
import { AssignmentsComponent } from 'app/overview/dispatcher/assignments/assignments.component';

@NgModule({
  imports: [SharedModule, MonacoEditorModule.forRoot()],
  declarations: [DispatcherComponent, AssignmentComponent, AssignmentsComponent],
  exports: [AssignmentComponent],
})
export class DispatcherModule {}
