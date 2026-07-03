import { ApplicationConfig, inject, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { catchError, of } from 'rxjs';

import { appRoutes } from './app.routes';
import { apiInterceptor } from './core/interceptors/api.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { ENVIRONMENT } from './core/tokens/environment.token';
import { environment } from '../environments/environment';
import { AuthService } from './core/services/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(appRoutes, withComponentInputBinding()),
    provideHttpClient(
      withInterceptors([apiInterceptor, errorInterceptor]),
      withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' }),
    ),
    provideAnimationsAsync(),
    { provide: MAT_DATE_LOCALE, useValue: 'en-US' },
    { provide: ENVIRONMENT, useValue: environment },
    // Populate AuthService.currentUser before the router activates the first
    // route, so authGuard has an answer immediately. A 401 here just means
    // "not logged in" — not an error worth surfacing.
    provideAppInitializer(() => {
      const auth = inject(AuthService);
      return auth.fetchMe().pipe(catchError(() => of(null)));
    }),
  ],
};
