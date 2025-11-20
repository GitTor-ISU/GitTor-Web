import { Routes } from '@angular/router';
import { About } from '@features/about/about';
import { AuthRoutes } from '@features/auth/auth-routes';
import { Home } from '@features/home/home';
import { NotFound } from '@features/not-found/not-found';
import { RepositoryList } from '@features/repository-list/repository-list';
import { Repository } from '@features/repository/repository';
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
  ...AuthRoutes,
  { path: '**', component: NotFound },
];
