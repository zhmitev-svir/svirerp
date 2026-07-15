import { Routes } from '@angular/router';

export const settingsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/settings-page/settings-page.component').then(m => m.SettingsPageComponent),
  },
];
