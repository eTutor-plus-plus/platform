import {Routes} from "@angular/router";
import {OverviewComponent} from "./overview.component";

/**
 * Overview related routes
 */
export const overviewRoute: Routes = [
  {
    path: '',
    component: OverviewComponent,
    data: {
      requireLogin: true
    }
  }
]
