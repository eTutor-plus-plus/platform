import { Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { Subscription } from 'rxjs';

import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginService } from 'app/login/login.service';
import { LOGIN_PATTERN } from 'app/shared/constants/user.constants';

/**
 * Component which represents the home / login page.
 */
@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('username', { static: false })
  public username?: ElementRef;

  public authenticationError = false;

  public loginForm = this.fb.group({
    username: ['', [Validators.required, Validators.pattern(LOGIN_PATTERN)]],
    password: ['', [Validators.required]],
    rememberMe: [false],
  });

  public account: Account | null = null;
  public authSubscription?: Subscription;

  private readonly OVERVIEW_ROUTE = '/overview';

  constructor(
    private accountService: AccountService,
    private fb: FormBuilder,
    private router: Router,
    private loginService: LoginService
  ) {}

  public ngOnInit(): void {
    if (this.isAuthenticated()) {
      this.router.navigate([this.OVERVIEW_ROUTE]);
    }

    this.authSubscription = this.accountService.getAuthenticationState().subscribe(account => (this.account = account));
  }

  public isAuthenticated(): boolean {
    return this.accountService.isAuthenticated();
  }

  public login(): void {
    this.loginService
      .login({
        username: this.loginForm.get('username')!.value,
        password: this.loginForm.get('password')!.value,
        rememberMe: this.loginForm.get('rememberMe')!.value,
      })
      .subscribe(
        () => {
          this.authenticationError = false;

          this.router.navigate([this.OVERVIEW_ROUTE]);
        },
        () => {
          this.authenticationError = true;
        }
      );
  }

  public requestResetPassword(): void {
    this.router.navigate(['/account/reset', 'request']);
  }

  public ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  public ngAfterViewInit(): void {
    if (this.username) {
      this.username.nativeElement.focus();
    }
  }
}
