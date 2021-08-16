import { Component } from '@angular/core';
import { AccountService } from '../core/auth/account.service';
import { Authority } from 'app/config/authority.constants';

/**
 * Component which displays the overview page for authenticated users.
 */
@Component({
  selector: 'jhi-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss'],
})
export class OverviewComponent {
  /**
   * Constructor.
   *
   * @param account the injected account service
   */
  constructor(private account: AccountService) {}

  /**
   * Returns whether the currently logged-in user is
   * a student or not.
   */
  public isUserStudent(): boolean {
    return this.account.hasAnyAuthority(Authority.STUDENT);
  }

  /**
   * Returns whether the currently logged-in user is
   * a lecturer / course instructor or not.
   */
  public isLecturer(): boolean {
    return this.account.hasAnyAuthority(Authority.INSTRUCTOR);
  }
}
