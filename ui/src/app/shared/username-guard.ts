import { CanMatchFn } from '@angular/router';

const reservedPaths = ['login', 'register', 'logout', 'about', 'help', 'contact', 'settings'];

export const usernameGuard: CanMatchFn = (route, segments) => {
  if (reservedPaths.includes(segments[0].path)) {
    return false;
  }

  return true;
};
