import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import ThemeService from '@core/theme-service';
import { NgxGridpatternComponent } from '@omnedia/ngx-gridpattern';
import { NgxTypewriterComponent } from '@omnedia/ngx-typewriter';
import { ThemeToggle } from '@shared/components/theme-toggle/theme-toggle';
import { LayoutComponent } from '@shared/components/z-layout';
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
    ThemeToggle,
    NgxTypewriterComponent,
    NgxGridpatternComponent,
    LayoutComponent,
  ],
  templateUrl: './auth-layout.html',
})
export class AuthLayout {
  protected readonly typeWriterWords = ['GitTor', 'Secure', 'Peer-to-Peer', 'Open-Source', 'GitTor'];
  protected readonly themeService = inject(ThemeService);
}
