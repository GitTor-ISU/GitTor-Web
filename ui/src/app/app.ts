import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from '@core/theme';
import { Navbar } from '@features/navbar/navbar';
import { HeartbeatService } from '@generated/openapi/services/heartbeat';
import { NgxSonnerToaster, toast } from 'ngx-sonner';
import { Subscription } from 'rxjs';

/**
 * Application component.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, NgxSonnerToaster],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  providers: [],
})
export class App implements OnInit, OnDestroy {
  private readonly heartbeatService = inject(HeartbeatService);
  private readonly themeService = inject(ThemeService);
  private subscriptions: Subscription[] = [];

  public ngOnInit(): void {
    this.themeService.initTheme();
    const sub = this.heartbeatService.heartbeat().subscribe({
      next: () =>
        toast.success('API: Connected', {
          description: 'Connected to API successfully.',
        }),
      error: () =>
        toast.error('API: Failed to connect', {
          description: 'Please check if API is running.',
        }),
    });

    this.subscriptions.push(sub);
  }

  public ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
  }

  protected getCurrentTheme(): 'light' | 'dark' {
    return this.themeService.getCurrentTheme();
  }
}
