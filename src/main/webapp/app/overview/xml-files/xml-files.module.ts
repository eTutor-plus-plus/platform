import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { XmlFileComponent } from './xml-file/xml-file.component';
import { xmlFilesRoute } from './xml-files.route';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';

/**
 * Module for displaying XML files
 */
@NgModule({
  imports: [SharedModule, OverviewSharedModule, RouterModule.forChild(xmlFilesRoute), MonacoEditorModule],
  declarations: [XmlFileComponent],
})
export class XmlFilesModule {}
