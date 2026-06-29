import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';
import { ApiError } from '../models/api.model';

/**
 * Global HTTP error handler.
 *
 * Shows a user-facing snack-bar message for each error category, then
 * re-throws the original error so component-level handlers can still react
 * (e.g. resetting a loading flag).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const apiError = err.error as ApiError | undefined;
      const message = apiError?.message ?? 'An unexpected error occurred';

      if (err.status === 0) {
        notifications.error('Cannot reach the server. Check your connection.');
      } else if (err.status >= 500) {
        notifications.error('Server error — please try again later.');
      } else if (err.status === 409) {
        notifications.error(message);
      } else if (err.status === 404) {
        notifications.error('Resource not found.');
      } else if (err.status === 400) {
        notifications.error(message);
      }

      return throwError(() => err);
    }),
  );
};
