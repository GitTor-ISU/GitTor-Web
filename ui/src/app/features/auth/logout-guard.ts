import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import SessionService from '@core/session-service';

export const logoutGuard: CanActivateFn = async () => {
  const sessionService = inject(SessionService);

  if (sessionService.hasToken()) {
    await sessionService.logout();
    window.location.reload();
  }

  return false;
};
