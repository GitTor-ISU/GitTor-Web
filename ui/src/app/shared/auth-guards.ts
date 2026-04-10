import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import SessionService from '../core/session-service';

export const guestOnlyGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (sessionService.hasToken()) {
    return router.parseUrl('/');
  }

  return true;
};

export const requireAuthGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (!sessionService.hasToken()) {
    return router.parseUrl('/login');
  }

  return true;
};
