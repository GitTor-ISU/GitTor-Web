import { ActivatedRouteSnapshot, Route } from '@angular/router';

export const DocsRoutes: Route[] = [
  {
    path: 'docs',
    runGuardsAndResolvers: 'always',
    resolve: {
      page: (route: ActivatedRouteSnapshot) => route.firstChild?.routeConfig?.path || 'installation',
    },
    loadComponent: () => import('./docs').then((m) => m.Docs),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'installation',
      },
      {
        path: 'installation',
        loadComponent: () => import('./installation/installation').then((m) => m.Installation),
        title: 'Installation',
      },
      {
        path: 'configuration',
        loadComponent: () => import('./config/config').then((m) => m.Config),
        title: 'Configuration',
      },
      {
        path: 'usage',
        loadComponent: () => import('./usage/usage').then((m) => m.Usage),
        title: 'Usage',
      },
      {
        path: 'faq',
        loadComponent: () => import('./faq/faq').then((m) => m.Faq),
        title: 'FAQ',
      },
    ],
  },
];
