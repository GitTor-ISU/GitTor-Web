import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from '@features/navbar/navbar';
import { HeartbeatService } from '@generated/openapi/services/heartbeat';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { Toast } from 'primeng/toast';
import { Subscription } from 'rxjs';

/**
 * Application component.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ButtonModule, Toast, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  providers: [MessageService],
})
export class App implements OnInit, OnDestroy {
  private messageService = inject(MessageService);
  private heartbeatService = inject(HeartbeatService);
  private subscriptions: Subscription[] = [];

  public ngOnInit(): void {
    const sub = this.heartbeatService.heartbeat().subscribe({
      next: () => console.log('API: Successfully connected'),
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Success',
          detail: 'API: Failed to connect',
        }),
    });

    this.subscriptions.push(sub);
  }

  public ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
  }
}
