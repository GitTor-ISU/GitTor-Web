import { Route } from '@angular/router';
import { guestOnlyGuard } from '@shared/auth-guards';
import { logoutGuard } from './logout-guard';

export const AuthRoutes: Route[] = [
  {
    path: 'login',
    loadComponent: () => import('../../layouts/auth-layout/auth-layout').then((m) => m.AuthLayout),
    canActivate: [guestOnlyGuard],
    children: [{ path: '', loadComponent: () => import('./login/login').then((m) => m.Login), title: 'Login' }],
  },
  {
    path: 'register',
    loadComponent: () => import('../../layouts/auth-layout/auth-layout').then((m) => m.AuthLayout),
    canActivate: [guestOnlyGuard],
    children: [
      { path: '', loadComponent: () => import('./register/register').then((m) => m.Register), title: 'Register' },
    ],
  },
  {
    path: 'logout',
    loadComponent: () => import('../../layouts/auth-layout/auth-layout').then((m) => m.AuthLayout),
    canActivate: [logoutGuard],
  },
];
