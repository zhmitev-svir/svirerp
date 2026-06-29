import { Routes } from '@angular/router';

export const governanceRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/governance-home/governance-home.component').then(
        m => m.GovernanceHomeComponent,
      ),
  },
];
