import { Routes } from '@angular/router';
import { About } from '@features/about/about';
import { AuthRoutes } from '@features/auth/auth-routes';
import { Home } from '@features/home/home';
import { NotFound } from '@features/not-found/not-found';
import { RepositoryList } from '@features/repository-list/repository-list';
import { Repository } from '@features/repository/repository';
import { usernameGuard } from '@shared/username-guard';
import { MainLayout } from './layouts/main-layout/main-layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    children: [
      { path: '', component: Home },
      { path: 'about', component: About, title: 'About' },
      {
        path: ':owner',
        canMatch: [usernameGuard],
        component: RepositoryList,
        title: (route) => `${route.params['owner']}`,
      },
      {
        path: ':owner/:name',
        canMatch: [usernameGuard],
        component: Repository,
        title: (route) => `${route.params['owner']}/${route.params['name']}`,
      },
    ],
  },
  ...AuthRoutes,
  { path: '**', component: NotFound, title: '404 Not Found' },
];
