import { SharedModule } from 'app/shared/shared.module';
import { NgModule } from '@angular/core';
import { MonacoEditorModule, NgxMonacoEditorConfig } from 'ngx-monaco-editor';
import { AssignmentComponent } from 'app/overview/dispatcher/assignment/assignment.component';
import { LecturerRunSubmissionComponent } from './lecturer-run-submission/lecturer-run-submission.component';
import { SafeHtmlPipe } from './assignment/safe-html-pipe';
import { myMonacoLoad } from './monaco-config';
import { PmAssignmentComponent } from './assignment-pm/pm.assignment.component';

/**
 * Module that contains "all" components related to the dispatcher, which is a seperate application
 * used to evaluate SQL, PM, XQUery, Datalog and Relational Algebra exercises
 */
const monacoConfig: NgxMonacoEditorConfig = {
  onMonacoLoad: myMonacoLoad,
};

@NgModule({
  imports: [SharedModule, MonacoEditorModule.forRoot(monacoConfig)],
  declarations: [AssignmentComponent, LecturerRunSubmissionComponent, SafeHtmlPipe, PmAssignmentComponent],
  exports: [AssignmentComponent, PmAssignmentComponent],
})
export class DispatcherModule {}
