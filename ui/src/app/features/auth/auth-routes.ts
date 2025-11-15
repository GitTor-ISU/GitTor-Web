import { Route } from '@angular/router';
import { authGuard } from '@features/auth/auth-guard';

export const AuthRoutes: Route[] = [
  {
    path: '',
    loadComponent: () => import('../../layouts/auth-layout/auth-layout').then((m) => m.AuthLayout),
    canActivateChild: [authGuard],
    children: [
      {
        path: 'login',
        loadComponent: () => import('./login/login').then((m) => m.Login),
      },
      {
        path: 'register',
        loadComponent: () => import('./register/register').then((m) => m.Register),
      },
    ],
  },
];
