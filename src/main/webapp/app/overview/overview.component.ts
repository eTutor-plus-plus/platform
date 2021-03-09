import { Component, OnInit } from '@angular/core';
import { AccountService } from '../core/auth/account.service';
import { Authority } from '../shared/constants/authority.constants';

/**
 * Component which displays the overview page for authenticated users.
 */
@Component({
  selector: 'jhi-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss'],
})
export class OverviewComponent implements OnInit {
  /**
   * Constructor.
   *
   * @param account the injected account service
   */
  constructor(private account: AccountService) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}

  /**
   * Returns whether the currently logged-in user is
   * a student or not.
   */
  public isUserStudent(): boolean {
    return this.account.hasAnyAuthority(Authority.STUDENT);
  }
}
