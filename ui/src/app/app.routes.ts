import { Routes } from '@angular/router';
import { About } from '@features/about/about';
import { Login } from '@features/auth/login/login';
import { Register } from '@features/auth/register/register';
import { Home } from '@features/home/home';
import { NotFound } from '@features/not-found/not-found';
import { RepositoryList } from '@features/repository-list/repository-list';
import { Repository } from '@features/repository/repository';
import { AuthLayout } from './layouts/auth-layout/auth-layout';
import { MainLayout } from './layouts/main-layout/main-layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    children: [
      { path: '', component: Home },
      { path: 'about', component: About },
      { path: ':owner/repositories', component: RepositoryList },
      { path: ':owner/:name', component: Repository },
    ],
  },
  {
    path: '',
    component: AuthLayout,
    children: [
      { path: 'register', component: Register },
      { path: 'login', component: Login },
    ],
  },
  { path: '**', component: NotFound },
];
