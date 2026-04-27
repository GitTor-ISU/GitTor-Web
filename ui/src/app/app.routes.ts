import { Routes } from '@angular/router';
import { About } from '@features/about/about';
import { AuthRoutes } from '@features/auth/auth-routes';
import { Home } from '@features/home/home';
import { NotFound } from '@features/not-found/not-found';
import { RepositoryList } from '@features/repository-list/repository-list';
import { RepositoryUpload } from '@features/repository-list/repository-upload/repository-upload';
import { Repository } from '@features/repository/repository';
import { SettingsRoutes } from '@features/settings/settings-routes';
import { validUserPathGuard } from '@shared/user-path-guards';
import { guestOnlyGuard, requireAuthGuard } from '@shared/auth-guards';
import { MainLayout } from './layouts/main-layout/main-layout';
import { currentUserResolver, profileResolver, profileTorrentsResolver } from './shared/user-resolvers';

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
      { path: 'new', component: RepositoryUpload, title: 'New repository', canActivate: [requireAuthGuard] },
      {
        path: ':owner',
        canMatch: [validUserPathGuard],
        resolve: { profile: profileResolver, torrents: profileTorrentsResolver },
        component: RepositoryList,
        title: (route) => `${route.params['owner']}`,
      },
      {
        path: ':owner/:name',
        canMatch: [validUserPathGuard],
        resolve: { profile: profileResolver },
        component: Repository,
        title: (route) => `${route.params['owner']}/${route.params['name']}`,
      },
      ...SettingsRoutes,
    ],
  },
  ...AuthRoutes,
  { path: '**', component: NotFound, title: '404 Not Found' },
];
