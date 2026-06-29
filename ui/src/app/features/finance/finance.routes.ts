import { Routes } from '@angular/router';

export const financeRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/finance-home/finance-home.component').then(m => m.FinanceHomeComponent),
  },
];
