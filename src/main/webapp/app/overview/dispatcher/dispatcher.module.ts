import { SharedModule } from 'app/shared/shared.module';
import { NgModule } from '@angular/core';
import { DispatcherAssignmentComponent } from 'app/overview/dispatcher/dispatcher-assignment/dispatcher-assignment.component';
import { DispatcherAssignmentModalComponent } from './dispatcher-assignment-modal/dispatcher-assignment-modal.component';
import { SafeHtmlPipe } from './dispatcher-assignment/safe-html-pipe';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';

/**
 * Module that contains "all" components related to the dispatcher, which is a seperate application
 * used to evaluate SQL, XQUery, Datalog and Relational Algebra exercises
 */

@NgModule({
  imports: [SharedModule, MonacoEditorModule],
  declarations: [DispatcherAssignmentComponent, DispatcherAssignmentModalComponent, SafeHtmlPipe],
  exports: [DispatcherAssignmentComponent],
})
export class DispatcherModule {}
