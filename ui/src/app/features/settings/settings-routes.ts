import { Route } from '@angular/router';
import { requireAuthGuard } from '@shared/auth-guards';
import { Settings } from './settings';

export const SettingsRoutes: Route[] = [
  {
    path: 'settings',
    canActivate: [requireAuthGuard],
    canDeactivate: [async (component: Settings): Promise<boolean> => await component.onDeselected()],
    loadComponent: () => import('./settings').then((m) => m.Settings),
  },
];
