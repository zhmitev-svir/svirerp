import { Routes } from '@angular/router';

export const membershipRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/membership-shell/membership-shell.component').then(
        m => m.MembershipShellComponent,
      ),
    children: [
      { path: '', redirectTo: 'types', pathMatch: 'full' },
      {
        path: 'types',
        loadComponent: () =>
          import('./pages/membership-type-list/membership-type-list.component').then(
            m => m.MembershipTypeListComponent,
          ),
      },
      {
        path: 'members',
        loadComponent: () =>
          import('./pages/member-list/member-list.component').then(
            m => m.MemberListComponent,
          ),
      },
    ],
  },
];
