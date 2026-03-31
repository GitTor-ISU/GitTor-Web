import { inject } from '@angular/core';
import { CanMatchFn } from '@angular/router';

import { UsersService } from '@generated/openapi/services/users';
import { firstValueFrom } from 'rxjs';

const reservedPaths = ['login', 'register', 'about', 'help', 'contact', 'settings'];

export const reservedPathGuard: CanMatchFn = (_route, segments) => {
  if (reservedPaths.includes(segments[0].path)) {
    return false;
  }

  return true;
};

export const validUserPathGuard: CanMatchFn = async (_route, segments) => {
  if (reservedPaths.includes(segments[0].path)) {
    return false;
  }

  const usersService = inject(UsersService);

  const user = await firstValueFrom(usersService.getUser(segments[0].path)).catch((e) => {
    console.error(e);
    return undefined;
  });

  return user !== undefined;
};
