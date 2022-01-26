import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { OverviewSharedModule } from '../shared/overview-shared.module';
import { RouterModule } from '@angular/router';
import { XmlFileComponent } from './xml-file/xml-file.component';
import { xmlFilesRoute } from './xml-files.route';

/**
 * Module for displaying XML files
 */
@NgModule({
  imports: [SharedModule, OverviewSharedModule, RouterModule.forChild(xmlFilesRoute)],
  declarations: [XmlFileComponent],
})
export class XmlFilesModule {}
