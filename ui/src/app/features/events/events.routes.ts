import { Routes } from '@angular/router';

export const eventsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/event-list/event-list.component').then(m => m.EventListComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/event-detail/event-detail.component').then(m => m.EventDetailComponent),
  },
];
