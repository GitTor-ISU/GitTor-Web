import { Route } from '@angular/router';
import { authGuard } from '@features/auth/auth-guard';

export const AuthRoutes: Route[] = [
  {
    path: 'login',
    loadComponent: () => import('../../layouts/auth-layout/auth-layout').then((m) => m.AuthLayout),
    canActivate: [authGuard],
    children: [{ path: '', loadComponent: () => import('./login/login').then((m) => m.Login), title: 'Login' }],
  },
  {
    path: 'register',
    loadComponent: () => import('../../layouts/auth-layout/auth-layout').then((m) => m.AuthLayout),
    canActivate: [authGuard],
    children: [
      { path: '', loadComponent: () => import('./register/register').then((m) => m.Register), title: 'Register' },
    ],
  },
];
