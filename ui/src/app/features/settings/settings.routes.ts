import { Routes } from '@angular/router';

export const settingsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/settings-shell/settings-shell.component').then(
        m => m.SettingsShellComponent,
      ),
    children: [
      { path: '', redirectTo: 'general', pathMatch: 'full' },
      {
        path: 'general',
        loadComponent: () =>
          import('./pages/settings-page/settings-page.component').then(
            m => m.SettingsPageComponent,
          ),
      },
      {
        path: 'organization',
        loadComponent: () =>
          import('./pages/organization-settings/organization-settings.component').then(
            m => m.OrganizationSettingsComponent,
          ),
      },
    ],
  },
];
