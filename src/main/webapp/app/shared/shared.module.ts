import { NgModule } from '@angular/core';
import { EtutorPlusPlusSharedLibsModule } from './shared-libs.module';
import { FindLanguageFromKeyPipe } from './language/find-language-from-key.pipe';
import { AlertComponent } from './alert/alert.component';
import { AlertErrorComponent } from './alert/alert-error.component';
import { LoginModalComponent } from './login/login.component';
import { HasAnyAuthorityDirective } from './auth/has-any-authority.directive';
import {TranslateRolePipe} from "app/shared/language/translate-role.pipe";

@NgModule({
  imports: [EtutorPlusPlusSharedLibsModule],
  declarations: [FindLanguageFromKeyPipe, TranslateRolePipe, AlertComponent, AlertErrorComponent, LoginModalComponent, HasAnyAuthorityDirective],
  entryComponents: [LoginModalComponent],
  exports: [
    EtutorPlusPlusSharedLibsModule,
    FindLanguageFromKeyPipe,
    TranslateRolePipe,
    AlertComponent,
    AlertErrorComponent,
    LoginModalComponent,
    HasAnyAuthorityDirective,
  ],
})
export class EtutorPlusPlusSharedModule {}
