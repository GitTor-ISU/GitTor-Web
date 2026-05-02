import { Component, computed, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import ThemeService from '@core/theme-service';
import { ZardLoaderComponent } from '@shared/components/z-loader/loader.component';
import { ZardToastComponent } from '@shared/components/z-toast/toast.component';

/**
 * Application component.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ZardToastComponent, ZardLoaderComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  providers: [],
})
export class App {
  protected readonly router = inject(Router);
  protected readonly isLoading = computed(() => !!this.router.currentNavigation());

  protected readonly themeService = inject(ThemeService);
}
