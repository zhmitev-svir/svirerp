import { Routes } from '@angular/router';

export const volunteersRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/volunteer-list/volunteer-list.component').then(
        m => m.VolunteerListComponent,
      ),
  },
];
