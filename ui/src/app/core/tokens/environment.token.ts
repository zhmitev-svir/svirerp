import { InjectionToken } from '@angular/core';

export interface AppEnvironment {
  production: boolean;
  apiUrl: string;
}

export const ENVIRONMENT = new InjectionToken<AppEnvironment>('APP_ENVIRONMENT');
