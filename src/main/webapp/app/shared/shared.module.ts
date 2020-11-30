import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedLibsModule } from './shared-libs.module';
import { FindLanguageFromKeyPipe } from './language/find-language-from-key.pipe';
import { AlertComponent } from './alert/alert.component';
import { AlertErrorComponent } from './alert/alert-error.component';
import { HasAnyAuthorityDirective } from './auth/has-any-authority.directive';
import { TranslateRolePipe } from 'app/shared/language/translate-role.pipe';
import { QuillModule } from 'ngx-quill';
import { TreeviewModule } from 'ngx-treeview';

@NgModule({
  imports: [
    EtutorPlusPlusSharedLibsModule,
    QuillModule.forRoot({
      modules: {
        toolbar: [
          ['bold', 'italic', 'underline', 'strike'],
          ['blockquote'],
          [{ header: [1, 2, 3, 4, 5, false] }],
          [{ list: 'ordered' }, { list: 'bullet' }],
          ['link'],
        ],
      },
    }),
    TreeviewModule.forRoot(),
  ],
  declarations: [FindLanguageFromKeyPipe, TranslateRolePipe, AlertComponent, AlertErrorComponent, HasAnyAuthorityDirective],
  exports: [
    EtutorPlusPlusSharedLibsModule,
    FindLanguageFromKeyPipe,
    TranslateRolePipe,
    AlertComponent,
    AlertErrorComponent,
    HasAnyAuthorityDirective,
    QuillModule,
    TreeviewModule,
  ],
})
export class EtutorPlusPlusSharedModule {}
