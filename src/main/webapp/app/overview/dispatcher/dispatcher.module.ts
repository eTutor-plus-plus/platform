import { SharedModule } from 'app/shared/shared.module';
import { NgModule } from '@angular/core';
import { MonacoEditorModule, NgxMonacoEditorConfig } from 'ngx-monaco-editor';
import { DispatcherAssignmentComponent } from 'app/overview/dispatcher/dispatcher-assignment/dispatcher-assignment.component';
import { DispatcherAssignmentModalComponent } from './dispatcher-assignment-modal/dispatcher-assignment-modal.component';
import { SafeHtmlPipe } from './dispatcher-assignment/safe-html-pipe';
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
  declarations: [DispatcherAssignmentComponent, DispatcherAssignmentModalComponent, SafeHtmlPipe, PmAssignmentComponent],
  exports: [DispatcherAssignmentComponent, PmAssignmentComponent],
})
export class DispatcherModule {}
