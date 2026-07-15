import { Routes } from '@angular/router';

export const governanceRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/governance-shell/governance-shell.component').then(
        m => m.GovernanceShellComponent,
      ),
    children: [
      { path: '', redirectTo: 'trustees', pathMatch: 'full' },
      {
        path: 'trustees',
        loadComponent: () =>
          import('./pages/trustee-list/trustee-list.component').then(
            m => m.TrusteeListComponent,
          ),
      },
    ],
  },
];
