import { Component } from '@angular/core';
import { AccountService } from '../../core/auth/account.service';
import { Authority } from 'app/config/authority.constants';

/**
 * Component which displays assignments for authenticated users.
 */
@Component({
  selector: 'jhi-dispatcher',
  templateUrl: './dispatcher.component.html',
  styleUrls: ['./dispatcher.component.scss'],
})
export class DispatcherComponent {
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
}
