import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from '@core/theme';
import { NgxBackgroundBeamsComponent } from '@omnedia/ngx-background-beams';
import { NgxTypewriterComponent } from '@omnedia/ngx-typewriter';
import { ThemeToggle } from '@shared/components/theme-toggle/theme-toggle';
import { LayoutModule } from '@shared/components/z-layout/layout.module';
import { ZardResizableHandleComponent } from '@shared/components/z-resizable/resizable-handle.component';
import { ZardResizablePanelComponent } from '@shared/components/z-resizable/resizable-panel.component';
import { ZardResizableComponent } from '@shared/components/z-resizable/resizable.component';
/**
 * Auth layout component.
 */
@Component({
  selector: 'app-auth-layout',
  imports: [
    RouterOutlet,
    CommonModule,
    ZardResizableComponent,
    ZardResizablePanelComponent,
    ZardResizableHandleComponent,
    LayoutModule,
    ThemeToggle,
    NgxBackgroundBeamsComponent,
    NgxTypewriterComponent,
  ],
  templateUrl: './auth-layout.html',
})
export class AuthLayout {
  protected readonly typeWriterWords = ['Secure', 'Peer-to-peer', 'Open', 'GitTor'];
  protected readonly themeService = inject(ThemeService);
}
