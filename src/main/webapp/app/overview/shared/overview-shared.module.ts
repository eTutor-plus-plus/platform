import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { LearningGoalTreeviewComponent } from './learning-goal-treeview/learning-goal-treeview.component';
import { FileUploadComponent } from 'app/overview/shared/file-upload/file-upload.component';
import { FileDownloadComponent } from 'app/overview/shared/file-download/file-download.component';

/**
 * Module for shared overview components.
 */
@NgModule({
  imports: [SharedModule],
  declarations: [LearningGoalTreeviewComponent, FileUploadComponent, FileDownloadComponent],
  exports: [LearningGoalTreeviewComponent, FileUploadComponent, FileDownloadComponent],
})
export class OverviewSharedModule {}
