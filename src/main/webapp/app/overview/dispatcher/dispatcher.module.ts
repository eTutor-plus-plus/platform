/**
 * Module for the dispatcher component.
 */
import { SharedModule } from 'app/shared/shared.module';
import { NgModule } from '@angular/core';
import { MonacoEditorModule } from 'ngx-monaco-editor';
import { AssignmentComponent } from 'app/overview/dispatcher/assignment/assignment.component';
import { LecturerRunSubmissionComponent } from './lecturer-run-submission/lecturer-run-submission.component';

@NgModule({
  imports: [SharedModule, MonacoEditorModule.forRoot()],
  declarations: [AssignmentComponent, LecturerRunSubmissionComponent],
  exports: [AssignmentComponent],
})
export class DispatcherModule {}
