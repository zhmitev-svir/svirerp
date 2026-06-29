import { Routes } from '@angular/router';

export const organizationsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/organization-list/organization-list.component').then(
        m => m.OrganizationListComponent,
      ),
  },
];
