import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import ThemeService from '@core/theme-service';
import { HeartbeatService } from '@generated/openapi/services/heartbeat';
import { ZardToastComponent } from '@shared/components/z-toast/toast.component';
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';

/**
 * Application component.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ZardToastComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  providers: [],
})
export class App implements OnInit {
  protected readonly router = inject(Router);

  protected readonly themeService = inject(ThemeService);
  private readonly heartbeatService = inject(HeartbeatService);

  public ngOnInit(): void {
    firstValueFrom(this.heartbeatService.heartbeat())
      .then(() => toast.success('API: Connected'))
      .catch(() => toast.error('API: Failed to connect'));
  }
}
