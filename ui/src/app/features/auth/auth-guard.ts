import { inject } from '@angular/core';
import { CanActivateChildFn, Router } from '@angular/router';
import SessionService from '../../core/session-service';

export const authGuard: CanActivateChildFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (sessionService.accessToken()) {
    return router.parseUrl('/');
  }

  return true;
};
