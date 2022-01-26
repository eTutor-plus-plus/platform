import { Routes } from '@angular/router';
import { XmlFileComponent } from './xml-file/xml-file.component';

/**
 * Routes realated to the {@link XmlFilesModule}
 */
export const xmlFilesRoute: Routes = [
  {
    path: '',
    component: XmlFileComponent,
  },
];
