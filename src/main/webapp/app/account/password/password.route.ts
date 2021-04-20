import { Route } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { PasswordComponent } from './password.component';
import { Authority } from 'app/config/authority.constants';

export const passwordRoute: Route = {
  path: 'password',
  component: PasswordComponent,
  data: {
    authorities: [Authority.USER, Authority.ADMIN, Authority.INSTRUCTOR, Authority.STUDENT, Authority.TUTOR],
    pageTitle: 'global.menu.account.password',
  },
  canActivate: [UserRouteAccessService],
};
