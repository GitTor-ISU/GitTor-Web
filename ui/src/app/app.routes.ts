import { Routes } from '@angular/router';
import { About } from '@features/about/about';
import { Register } from '@features/auth/register/register';
import { Home } from '@features/home/home';
import { NotFound } from '@features/not-found/not-found';
import { AuthLayout } from './layouts/auth-layout/auth-layout';
import { MainLayout } from './layouts/main-layout/main-layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    children: [
      { path: '', component: Home },
      { path: 'about', component: About },
    ],
  },
  {
    path: '',
    component: AuthLayout,
    children: [{ path: 'register', component: Register }],
  },
  { path: '**', component: NotFound },
];
