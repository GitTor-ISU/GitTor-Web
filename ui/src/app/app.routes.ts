import { Routes } from '@angular/router';
import { About } from '@features/about/about';
import { AboutRoutes } from '@features/about/about-routes';
import { AuthRoutes } from '@features/auth/auth-routes';
import { Home } from '@features/home/home';
import { NotFound } from '@features/not-found/not-found';
import { RepositoryList } from '@features/repository-list/repository-list';
import { Repository } from '@features/repository/repository';
import { SettingsRoutes } from '@features/settings/settings-routes';
import { guestOnlyGuard } from '@shared/auth-guards';
import { usernameGuard } from '@shared/username-guard';
import { MainLayout } from './layouts/main-layout/main-layout';
import { currentUserResolver } from './shared/current-user-resolver';

export const routes: Routes = [
  {
    path: '',
    component: Home,
    canActivate: [guestOnlyGuard],
  },
  {
    path: '',
    component: MainLayout,
    resolve: { user: currentUserResolver },
    children: [
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
      ...AboutRoutes,
      ...SettingsRoutes,
    ],
  },
  ...AuthRoutes,
  { path: '**', component: NotFound, title: '404 Not Found' },
];
