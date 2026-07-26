import { inject, Injectable, signal } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { catchError, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly oidcSecurityService = inject(OidcSecurityService);

  isAuthenticated = signal<boolean>(false);
  userData = signal<any>(null);

  constructor() {
    this.checkAuth();
  }

  checkAuth() {
    this.oidcSecurityService
      .checkAuth()
      .pipe(
        catchError((err) => {
          return of({ isAuthenticated: false, userData: null });
        }),
      )
      .subscribe(({ isAuthenticated, userData }) => {
        this.isAuthenticated.set(isAuthenticated);
        this.userData.set(userData);
      });
  }

  login(): void {
    this.oidcSecurityService.authorize();
  }

  logout(): void {
    this.oidcSecurityService.logoff().subscribe((result) => console.log(result));
  }
}
