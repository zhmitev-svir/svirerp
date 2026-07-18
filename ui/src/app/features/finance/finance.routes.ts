import { Routes } from '@angular/router';

export const financeRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/finance-shell/finance-shell.component').then(m => m.FinanceShellComponent),
    children: [
      { path: '', redirectTo: 'transactions', pathMatch: 'full' },
      {
        path: 'transactions',
        loadComponent: () =>
          import('./pages/transaction-list/transaction-list.component').then(
            m => m.TransactionListComponent,
          ),
      },
      {
        path: 'projects',
        loadComponent: () =>
          import('./pages/fund-list/fund-list.component').then(m => m.FundListComponent),
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./pages/account-list/account-list.component').then(m => m.AccountListComponent),
      },
      {
        path: 'vendors',
        loadComponent: () =>
          import('./pages/vendor-list/vendor-list.component').then(m => m.VendorListComponent),
      },
      {
        path: 'service-requests',
        loadComponent: () =>
          import('./pages/service-request-list/service-request-list.component').then(
            m => m.ServiceRequestListComponent,
          ),
      },
    ],
  },
];
