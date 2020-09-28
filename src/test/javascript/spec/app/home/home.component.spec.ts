import { ComponentFixture, TestBed, async, inject, fakeAsync, tick } from '@angular/core/testing';

import { EtutorPlusPlusTestModule } from '../../test.module';
import { HomeComponent } from 'app/home/home.component';
import { AccountService } from 'app/core/auth/account.service';
import { FormBuilder } from "@angular/forms";
import { LoginService } from 'app/core/login/login.service';
import { MockLoginService } from "../../helpers/mock-login.service";
import { MockRouter } from "../../helpers/mock-route.service";
import { Router } from "@angular/router";

describe('Component Tests', () => {
  describe('Home Component', () => {
    let comp: HomeComponent;
    let fixture: ComponentFixture<HomeComponent>;
    let accountService: AccountService;
    let mockLoginService: MockLoginService;
    let mockRouter: MockRouter;

    beforeEach(async(() => {
      TestBed.configureTestingModule({
        imports: [EtutorPlusPlusTestModule],
        declarations: [HomeComponent],
        providers: [FormBuilder, {
          provide: LoginService,
          useClass: MockLoginService
        }]
      })
        .overrideTemplate(HomeComponent, '')
        .compileComponents();
    }));

    beforeEach(() => {
      fixture = TestBed.createComponent(HomeComponent);
      comp = fixture.componentInstance;
      accountService = TestBed.inject(AccountService);
      mockLoginService = TestBed.inject(LoginService) as any;
      mockRouter = TestBed.inject(Router) as any;
    });

    it('Should call accountService.getAuthenticationState on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(accountService.getAuthenticationState).toHaveBeenCalled();
    });

    it('Should call accountService.isAuthenticated when it checks authentication', () => {
      // WHEN
      comp.isAuthenticated();

      // THEN
      expect(accountService.isAuthenticated).toHaveBeenCalled();
    });

    it('Should authenticate the user', inject([], fakeAsync(() => {
      // GIVEN
      const credentials = {
        username: 'admin',
        password: 'admin',
        rememberMe: true
      };

      comp.loginForm.patchValue({
        username: 'admin',
        password: 'admin',
        rememberMe: true
      });
      mockLoginService.setResponse({});

      // WHEN
      comp.login();
      tick();

      // THEN
      expect(comp.authenticationError).toEqual(false);
      expect(mockLoginService.loginSpy).toHaveBeenCalledWith(credentials)
    })));

    it('Should redirect user when request password', () => {
      // WHEN
      comp.requestResetPassword();

      // THEN
      expect(mockRouter.navigateSpy).toHaveBeenCalledWith(['/account/reset', 'request']);
    });
  });
});
