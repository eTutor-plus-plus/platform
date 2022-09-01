import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { errorRoute } from './layouts/error/error.route';
import { navbarRoute } from './layouts/navbar/navbar.route';
import { DEBUG_INFO_ENABLED } from 'app/app.constants';
import { Authority } from 'app/config/authority.constants';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

const LAYOUT_ROUTES = [navbarRoute, ...errorRoute];

@NgModule({
  imports: [
    RouterModule.forRoot(
      [
        {
          path: 'admin',
          data: {
            authorities: [Authority.ADMIN],
          },
          canActivate: [UserRouteAccessService],
          loadChildren: () => import('./admin/admin-routing.module').then(m => m.AdminRoutingModule),
        },
        {
          path: 'account',
          loadChildren: () => import('./account/account.module').then(m => m.AccountModule),
        },
        {
          path: 'overview',
          data: {
            requireLogin: true,
          },
          canActivate: [UserRouteAccessService],
          loadChildren: () => import('./overview/overview.module').then(m => m.OverviewModule),
        },
        {
          path: 'sql-tables',
          loadChildren: () => import('./overview/sql-tables/sql-tables.module').then(m => m.SqlTablesModule),
        },
        {
          path: 'datalog-facts',
          loadChildren: () => import('./overview/datalog-facts/datalog-facts.module').then(m => m.DatalogFactsModule),
        },
        {
          path: 'XML',
          loadChildren: () => import('./overview/xml-files/xml-files.module').then(m => m.XmlFilesModule),
        },
        ...LAYOUT_ROUTES,
      ],
      { enableTracing: DEBUG_INFO_ENABLED }
    ),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
