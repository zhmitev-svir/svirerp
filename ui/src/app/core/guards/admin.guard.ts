import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Restricts a route to the local admin — never a Google-authenticated user,
 * even though both pass authGuard. Assumes authGuard already ran (this
 * doesn't redirect to /login on its own if currentUser is null). */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.currentUser()?.provider === 'local-admin') {
    return true;
  }
  return router.createUrlTree(['/dashboard']);
};
