import { Routes } from '@angular/router';
import { XmlFileComponent } from './xml-file/xml-file.component';

/**
 * Tasks related routes
 */
export const xmlFilesRoute: Routes = [
  {
    path: '',
    component: XmlFileComponent,
  },
];
