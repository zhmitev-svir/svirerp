import { Routes } from '@angular/router';

export const membershipRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/membership-home/membership-home.component').then(
        m => m.MembershipHomeComponent,
      ),
  },
];
