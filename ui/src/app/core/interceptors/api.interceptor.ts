import { HttpInterceptorFn } from '@angular/common/http';

/** Adds common request headers to every outbound API call. */
export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const cloned = req.clone({
    setHeaders: { Accept: 'application/json' },
  });
  return next(cloned);
};
