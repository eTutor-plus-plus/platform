/**
 * Module for the dispatcher component.
 */
import { SharedModule } from 'app/shared/shared.module';
import { NgModule } from '@angular/core';
import { MonacoEditorModule } from 'ngx-monaco-editor';
import { DispatcherComponent } from 'app/overview/dispatcher/dispatcher.component';
import { AssignmentComponent } from 'app/overview/dispatcher/assignment/assignment.component';
import { RouterModule } from '@angular/router';
import { dispatcherRoute } from './dispatcher.route';

@NgModule({
  imports: [SharedModule, MonacoEditorModule.forRoot(), RouterModule.forChild(dispatcherRoute)],
  declarations: [DispatcherComponent, AssignmentComponent],
  exports: [AssignmentComponent],
})
export class DispatcherModule {}
