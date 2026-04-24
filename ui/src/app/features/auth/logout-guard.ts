import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import SessionService from '@core/session-service';

export const logoutGuard: CanActivateFn = async () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (sessionService.hasToken()) {
    await sessionService.logout();
  }

  return router.parseUrl('/');
};
