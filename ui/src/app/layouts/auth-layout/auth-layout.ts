import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NgxFlickeringGridComponent } from '@omnedia/ngx-flickering-grid';
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
    NgxFlickeringGridComponent,
  ],
  templateUrl: './auth-layout.html',
})
export class AuthLayout {}
