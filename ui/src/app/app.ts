import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import SessionService from '@core/session-service';
import ThemeService from '@core/theme-service';
import { HeartbeatService } from '@generated/openapi/services/heartbeat';
import { UsersService } from '@generated/openapi/services/users';
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
  protected readonly themeService = inject(ThemeService);
  private readonly sessionService = inject(SessionService);
  private readonly heartbeatService = inject(HeartbeatService);
  private readonly usersService = inject(UsersService);

  public ngOnInit(): void {
    firstValueFrom(this.heartbeatService.heartbeat())
      .then(() => toast.success('API: Connected'))
      .catch(() => toast.error('API: Failed to connect'));

    firstValueFrom(this.usersService.getMe()).then((user) => {
      this.sessionService.user.set(user);
      toast.success('Authorized');
    });
  }
}
