import { Routes } from '@angular/router';
import { About } from '@features/about/about';
import { AuthRoutes } from '@features/auth/auth-routes';
import { Home } from '@features/home/home';
import { NotFound } from '@features/not-found/not-found';
import { RepositoryList } from '@features/repositories/list/repository-list';
import { RepositoryUpload } from '@features/repositories/upload/repository-upload';
import { SettingsRoutes } from '@features/settings/settings-routes';
import { guestOnlyGuard, requireAuthGuard } from '@shared/auth-guards';
import { reservedPathGuard } from '@shared/user-path-guards';
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
        canMatch: [reservedPathGuard],
        resolve: { profile: profileResolver, torrents: profileTorrentsResolver },
        component: RepositoryList,
        title: (route) => `${route.params['owner']}`,
      },
      ...SettingsRoutes,
    ],
  },
  ...AuthRoutes,
  { path: '404', component: NotFound, title: '404 Not Found' },
  { path: '**', redirectTo: '404' },
];
