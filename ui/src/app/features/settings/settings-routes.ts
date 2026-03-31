import { ActivatedRouteSnapshot, Route } from '@angular/router';
import { requireAuthGuard } from '@shared/auth-guards';
import { Settings } from './settings';

export const SettingsRoutes: Route[] = [
  {
    path: 'settings',
    canActivate: [requireAuthGuard],
    canDeactivate: [async (component: Settings): Promise<boolean> => await component.onDeselected()],
    runGuardsAndResolvers: 'always',
    resolve: {
      page: (route: ActivatedRouteSnapshot) => route.firstChild?.routeConfig?.path || 'profile',
    },
    loadComponent: () => import('./settings').then((m) => m.Settings),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'profile',
      },
      {
        path: 'profile',
        loadComponent: () => import('./profile-settings/profile-settings').then((m) => m.ProfileSettings),
        title: 'Profile Settings',
      },
      {
        path: 'repository',
        loadComponent: () => import('./repository-settings/repository-settings').then((m) => m.RepositorySettings),
        title: 'Repository Settings',
      },
      {
        path: 'gpg',
        loadComponent: () => import('./gpg-settings/gpg-settings').then((m) => m.GpgSettings),
        title: 'GPG Settings',
      },
    ],
  },
];
