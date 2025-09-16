import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from '@features/navbar/navbar';
import { HeartbeatService } from '@generated/openapi/services/heartbeat';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { Toast } from 'primeng/toast';

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
export class App implements OnInit {
  private messageService = inject(MessageService);
  private heartbeatService = inject(HeartbeatService);

  public ngOnInit(): void {
    this.heartbeatService.heartbeat().subscribe({
      next: () => console.log('API: Successfully connected'),
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Success',
          detail: 'API: Failed to connect',
        }),
    });
  }
}
