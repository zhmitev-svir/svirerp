import { Routes } from '@angular/router';
import { ShellComponent } from './layout/shell/shell.component';

export const appRoutes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'persons',
        loadChildren: () =>
          import('./features/persons/persons.routes').then(m => m.personsRoutes),
      },
      {
        path: 'organizations',
        loadChildren: () =>
          import('./features/organizations/organizations.routes').then(m => m.organizationsRoutes),
      },
      {
        path: 'membership',
        loadChildren: () =>
          import('./features/membership/membership.routes').then(m => m.membershipRoutes),
      },
      {
        path: 'governance',
        loadChildren: () =>
          import('./features/governance/governance.routes').then(m => m.governanceRoutes),
      },
      {
        path: 'volunteers',
        loadChildren: () =>
          import('./features/volunteers/volunteers.routes').then(m => m.volunteersRoutes),
      },
      {
        path: 'events',
        loadChildren: () =>
          import('./features/events/events.routes').then(m => m.eventsRoutes),
      },
      {
        path: 'finance',
        loadChildren: () =>
          import('./features/finance/finance.routes').then(m => m.financeRoutes),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
