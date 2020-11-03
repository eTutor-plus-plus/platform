import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedLibsModule } from './shared-libs.module';
import { FindLanguageFromKeyPipe } from './language/find-language-from-key.pipe';
import { AlertComponent } from './alert/alert.component';
import { AlertErrorComponent } from './alert/alert-error.component';
import { HasAnyAuthorityDirective } from './auth/has-any-authority.directive';
import { TranslateRolePipe } from "app/shared/language/translate-role.pipe";

@NgModule({
  imports: [EtutorPlusPlusSharedLibsModule],
  declarations: [FindLanguageFromKeyPipe, TranslateRolePipe, AlertComponent, AlertErrorComponent, HasAnyAuthorityDirective],
  exports: [
    EtutorPlusPlusSharedLibsModule,
    FindLanguageFromKeyPipe,
    TranslateRolePipe,
    AlertComponent,
    AlertErrorComponent,
    HasAnyAuthorityDirective,
  ],
})
export class EtutorPlusPlusSharedModule {}
