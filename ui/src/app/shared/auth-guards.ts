import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import SessionService from '../core/session-service';

export const guestOnlyGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (!sessionService.hasToken()) {
    return true;
  }

  const currentUser = sessionService.user();
  if (currentUser) {
    return router.parseUrl(`/${currentUser.username}`);
  }

  return sessionService.fetchMe$().pipe(
    map((user) => {
      if (user) {
        return router.parseUrl(`/${user.username}`);
      }

      return true;
    })
  );
};

export const requireAuthGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (!sessionService.hasToken()) {
    return router.parseUrl('/login');
  }

  return true;
};
