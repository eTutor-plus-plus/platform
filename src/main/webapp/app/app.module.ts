import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import './vendor';
import { EtutorPlusPlusSharedModule } from 'app/shared/shared.module';
import { EtutorPlusPlusCoreModule } from 'app/core/core.module';
import { EtutorPlusPlusAppRoutingModule } from './app-routing.module';
import { EtutorPlusPlusHomeModule } from './home/home.module';
import { EtutorPlusPlusEntityModule } from './entities/entity.module';
// jhipster-needle-angular-add-module-import JHipster will add new module here
import { MainComponent } from './layouts/main/main.component';
import { NavbarComponent } from './layouts/navbar/navbar.component';
import { FooterComponent } from './layouts/footer/footer.component';
import { PageRibbonComponent } from './layouts/profiles/page-ribbon.component';
import { ActiveMenuDirective } from './layouts/navbar/active-menu.directive';
import { ErrorComponent } from './layouts/error/error.component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { QuillModule } from 'ngx-quill';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

@NgModule({
  imports: [
    BrowserModule,
    EtutorPlusPlusSharedModule,
    EtutorPlusPlusCoreModule,
    EtutorPlusPlusHomeModule,
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    QuillModule.forRoot({
      theme: 'snow',
    }),
    BrowserAnimationsModule,
    // jhipster-needle-angular-add-module JHipster will add new module here
    EtutorPlusPlusEntityModule,
    EtutorPlusPlusAppRoutingModule,
  ],
  declarations: [MainComponent, NavbarComponent, ErrorComponent, PageRibbonComponent, ActiveMenuDirective, FooterComponent],
  bootstrap: [MainComponent],
})
export class EtutorPlusPlusAppModule {}
