import { NgModule } from '@angular/core';
import { SharedLibsModule } from './shared-libs.module';
import { FindLanguageFromKeyPipe } from './language/find-language-from-key.pipe';
import { TranslateDirective } from './language/translate.directive';
import { AlertComponent } from './alert/alert.component';
import { AlertErrorComponent } from './alert/alert-error.component';
import { HasAnyAuthorityDirective } from './auth/has-any-authority.directive';
import { TranslateRolePipe } from 'app/shared/language/translate-role.pipe';
import { QuillModule } from 'ngx-quill';
import { TreeviewModule } from 'ngx-treeview';
import { ContextMenuModule } from 'ngx-contextmenu';
import { DurationPipe } from './date/duration.pipe';
import { FormatMediumDatetimePipe } from './date/format-medium-datetime.pipe';
import { FormatMediumDatePipe } from './date/format-medium-date.pipe';
import { SortByDirective } from './sort/sort-by.directive';
import { SortDirective } from './sort/sort.directive';
import { ItemCountComponent } from './pagination/item-count.component';

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
    ContextMenuModule,
  ],
  declarations: [FindLanguageFromKeyPipe, TranslateRolePipe, AlertComponent, AlertErrorComponent, HasAnyAuthorityDirective],
  exports: [
    SharedLibsModule,
    FindLanguageFromKeyPipe,
    TranslateRolePipe,
TranslateDirective,
    AlertComponent,
    AlertErrorComponent,
    HasAnyAuthorityDirective,
    QuillModule,
    TreeviewModule,
    ContextMenuModule,
    DurationPipe,
    FormatMediumDatetimePipe,
    FormatMediumDatePipe,
    SortByDirective,
    SortDirective,
    ItemCountComponent,
  ],
})
export class SharedModule {}
