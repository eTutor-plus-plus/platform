import { Route } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { SettingsComponent } from './settings.component';
import { Authority } from 'app/config/authority.constants';

export const settingsRoute: Route = {
  path: 'settings',
  component: SettingsComponent,
  data: {
    authorities: [Authority.USER, Authority.ADMIN, Authority.INSTRUCTOR, Authority.STUDENT, Authority.TUTOR],
    pageTitle: 'global.menu.account.settings',
  },
  canActivate: [UserRouteAccessService],
};
