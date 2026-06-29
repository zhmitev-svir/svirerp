import { Routes } from '@angular/router';

export const personsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/person-list/person-list.component').then(m => m.PersonListComponent),
  },
];
