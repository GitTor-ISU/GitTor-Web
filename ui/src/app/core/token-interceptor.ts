import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, from, switchMap, throwError } from 'rxjs';
import SessionService from './session-service';

const addAuthHeader = (req: HttpRequest<unknown>, token: string | undefined): HttpRequest<unknown> => {
  return req.clone({
    setHeaders: { Authorization: token ? `Bearer ${token}` : '' },
  });
};

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionService: SessionService = inject(SessionService);

  if (req.url.startsWith('/api/authenticate')) {
    return next(req);
  }

  return next(addAuthHeader(req, sessionService.accessToken()?.accessToken)).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401) return throwError(() => error);
      return from(sessionService.refresh()).pipe(
        switchMap(() => {
          return next(addAuthHeader(req, sessionService.accessToken()?.accessToken));
        }),
        catchError((error: HttpErrorResponse) => {
          return throwError(() => error);
        })
      );
    })
  );
};
