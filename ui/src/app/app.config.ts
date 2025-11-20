import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  ErrorHandler,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { GlobalErrorHandler } from '@core/global-error-handler';
import { tokenInterceptor } from '@core/token-interceptor';
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
    withCredentials: true,
  });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([tokenInterceptor])),
    {
      provide: Configuration,
      useFactory: apiConfigFactory,
    },
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
  ],
};
