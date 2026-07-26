import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor, provideAuth } from 'angular-auth-oidc-client';
import { environment } from '../environments/environment';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideAuth({
      config: {
        authority: environment.auth.authority,
        redirectUrl: environment.auth.redirectUrl,
        postLogoutRedirectUri: environment.auth.postLogoutRedirectUri,
        clientId: environment.auth.clientId,
        scope: environment.auth.scope,
        responseType: environment.auth.responseType,
        silentRenew: environment.auth.silentRenew,
        useRefreshToken: environment.auth.useRefreshToken,
        logLevel: environment.auth.logLevel,
        secureRoutes: [environment.apiUrl],
      },
    }),

    provideHttpClient(withInterceptors([authInterceptor()]))
  ],
};
