import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';

import { Configuration } from '@generated/openapi/configuration';
import { routes } from './app.routes';

/**
 * Factory function to create API configuration.
 *
 * @returns {Configuration} The API configuration instance.
 */
export function apiConfigFactory(): Configuration {
  return new Configuration({
    basePath: '/api',
  });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    provideAnimationsAsync(),
    {
      provide: Configuration,
      useFactory: apiConfigFactory,
    },
  ],
};
