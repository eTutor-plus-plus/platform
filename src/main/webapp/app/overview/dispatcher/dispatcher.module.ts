/**
 * Module for the dispatcher component.
 */
import { SharedModule } from 'app/shared/shared.module';
import { NgModule } from '@angular/core';
import { MonacoEditorModule } from 'ngx-monaco-editor';

import { DispatcherComponent } from 'app/overview/dispatcher/dispatcher.component';
import { AssignmentComponent } from 'app/overview/dispatcher/assignment/assignment.component';

@NgModule({
  imports: [SharedModule, MonacoEditorModule.forRoot()],
  declarations: [DispatcherComponent, AssignmentComponent],
  exports: [AssignmentComponent],
})
export class DispatcherModule {}
