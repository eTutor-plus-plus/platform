import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, Observable } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/config/pagination.constants';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { UserManagementService } from '../service/user-management.service';
import { IUser, User } from '../user-management.model';
import { UserManagementDeleteDialogComponent } from '../delete/user-management-delete-dialog.component';
import { AlertService } from '../../../core/util/alert.service';

@Component({
  selector: 'jhi-user-mgmt',
  templateUrl: './user-management.component.html',
})
export class UserManagementComponent implements OnInit {
  currentAccount: Account | null = null;
  users: User[] | null = null;
  isLoading = false;
  totalItems = 0;
  itemsPerPage = ITEMS_PER_PAGE;
  page!: number;
  predicate!: string;
  ascending!: boolean;

  isRemovingUsers = false;
  searchString = '';
  appliedSearchString?: string = undefined;

  constructor(
    private userService: UserManagementService,
    private accountService: AccountService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private modalService: NgbModal,
    private alertService: AlertService
  ) {}

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => (this.currentAccount = account));
    this.handleNavigation();
  }

  setActive(user: User, isActivated: boolean): void {
    this.userService.update({ ...user, activated: isActivated }).subscribe(() => this.loadAll());
  }

  trackIdentity(index: number, item: User): number {
    return item.id!;
  }

  deleteUser(user: User): void {
    const modalRef = this.modalService.open(UserManagementDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.user = user;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed.subscribe(reason => {
      if (reason === 'deleted') {
        this.loadAll();
      }
    });
  }

  loadAll(): void {
    this.isLoading = true;

    let userObservable: Observable<HttpResponse<IUser[]>>;

    if (this.searchString !== this.appliedSearchString) {
      // Reset page when there has been a change since the last query.
      this.page = 1;
    }

    if (this.searchString === '') {
      this.appliedSearchString = undefined;

      userObservable = this.userService.query({
        page: this.page - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
      });
    } else {
      this.appliedSearchString = this.searchString.trim();

      userObservable = this.userService.queryFiltered(this.appliedSearchString, {
        page: this.page - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
      });
    }

    userObservable.subscribe(
      (res: HttpResponse<User[]>) => {
        this.isLoading = false;
        this.onSuccess(res.body, res.headers);
      },
      () => (this.isLoading = false)
    );
  }

  transition(): void {
    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute.parent,
      queryParams: {
        page: this.page,
        sort: `${this.predicate},${this.ascending ? ASC : DESC}`,
      },
    });
  }

  /**
   * Removes all inactive users.
   */
  public removeAllInactiveUsers(): void {
    this.isRemovingUsers = true;

    this.userService.deleteDeactivatedUsers().subscribe(
      value => {
        this.alertService.addAlert({
          type: 'success',
          translationKey: 'userManagement.inactiveRemoved',
          translationParams: { count: value },
          timeout: 5000,
        });

        if (value > 0) {
          this.loadAll();
        }

        this.isRemovingUsers = false;
      },
      () => {
        this.isRemovingUsers = false;
      }
    );
  }

  private handleNavigation(): void {
    combineLatest([this.activatedRoute.data, this.activatedRoute.queryParamMap]).subscribe(([data, params]) => {
      const page = params.get('page');
      this.page = +(page ?? 1);
      const sort = (params.get(SORT) ?? data['defaultSort']).split(',');
      this.predicate = sort[0];
      this.ascending = sort[1] === ASC;
      this.loadAll();
    });
  }

  private sort(): string[] {
    const result = [`${this.predicate},${this.ascending ? ASC : DESC}`];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  private onSuccess(users: User[] | null, headers: HttpHeaders): void {
    this.totalItems = Number(headers.get('X-Total-Count'));
    this.users = users;
  }
}
