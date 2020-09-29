import { Routes } from "@angular/router";
import { OverviewComponent } from "./overview.component";
import { UserRouteAccessService } from "../core/auth/user-route-access-service";
import { Authority } from "../shared/constants/authority.constants";

/**
 * Overview related routes.
 */
export const overviewRoute: Routes = [
  {
    path: '',
    component: OverviewComponent
  },
  {
    path: 'learning-goals',
    canActivate: [UserRouteAccessService],
    data: {
      authorities: [Authority.INSTRUCTOR]
    },
    loadChildren: () => import('./learning-goals/learning-goals.module').then(m => m.LearningGoalsModule)
  }
]
