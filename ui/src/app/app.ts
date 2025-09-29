import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from '@core/theme';
import { Navbar } from '@features/navbar/navbar';
import { HeartbeatService } from '@generated/openapi/services/heartbeat';
import { NgxSonnerToaster, toast } from 'ngx-sonner';

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
export class App implements OnInit {
  private readonly heartbeatService = inject(HeartbeatService);
  private readonly themeService = inject(ThemeService);

  public ngOnInit(): void {
    this.themeService.initTheme();
    this.heartbeatService.heartbeat().subscribe({
      next: () =>
        toast.success('API: Connected', {
          description: 'Connected to API successfully.',
        }),
      error: () =>
        toast.error('API: Failed to connect', {
          description: 'Please check if API is running.',
        }),
    });
  }

  protected getCurrentTheme(): 'light' | 'dark' {
    return this.themeService.getCurrentTheme();
  }
}
