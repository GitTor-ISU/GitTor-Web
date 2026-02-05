import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import SessionService from '@core/session-service';
import { Logo } from '@shared/components/logo/logo';
import { ThemeToggle } from '@shared/components/theme-toggle/theme-toggle';
import { ZardAvatarComponent } from '@shared/components/z-avatar';
import {
  ZardBreadcrumbComponent,
  ZardBreadcrumbItemComponent,
} from '@shared/components/z-breadcrumb/breadcrumb.component';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardDividerComponent } from '@shared/components/z-divider/divider.component';
import { ZardIconComponent } from '@shared/components/z-icon';
import {
  ContentComponent,
  LayoutComponent,
  SidebarComponent,
  SidebarGroupComponent,
  SidebarGroupLabelComponent,
} from '@shared/components/z-layout';
import { MenuItem } from '@shared/components/z-menu/menu-item.directive';
import { ZardMenuModule } from '@shared/components/z-menu/menu.module';
import { ZardSkeletonComponent } from '@shared/components/z-skeleton';
import { ZardTooltipImports } from '@shared/components/z-tooltip';
import { FolderIcon, HouseIcon, InfoIcon, LogInIcon, SearchIcon } from 'lucide-angular';

/**
 * Main layout component.
 */
@Component({
  selector: 'app-main-layout',
  imports: [
    ZardBreadcrumbItemComponent,
    ZardBreadcrumbComponent,
    ZardDividerComponent,
    ZardIconComponent,
    ZardMenuModule,
    ZardSkeletonComponent,
    ZardButtonComponent,
    ZardAvatarComponent,
    RouterOutlet,
    SidebarGroupLabelComponent,
    SidebarGroupComponent,
    SidebarComponent,
    LayoutComponent,
    ContentComponent,
    ZardTooltipImports,
    Logo,
    ThemeToggle,
  ],
  templateUrl: './main-layout.html',
})
export class MainLayout {
  protected readonly sessionService = inject(SessionService);
  protected readonly sidebarCollapsed = signal(true);
  protected readonly outletActivated = signal(false);
  protected logInIcon = LogInIcon;

  protected mainMenuItems: MenuItem[] = [
    { icon: HouseIcon, label: 'Home', route: '/' },
    { icon: InfoIcon, label: 'About', route: '/about' },
    { icon: SearchIcon, label: 'Search' },
  ];

  protected workspaceMenuItems: MenuItem[] = [
    {
      icon: FolderIcon,
      label: 'Repositories',
    },
  ];

  protected toggleSidebar(): void {
    this.sidebarCollapsed.update((collapsed) => !collapsed);
  }

  protected onOutletActivate(): void {
    this.outletActivated.set(true);
  }

  protected onOutletDeactivate(): void {
    this.outletActivated.set(false);
  }

  protected onCollapsedChange(collapsed: boolean): void {
    this.sidebarCollapsed.set(collapsed);
  }
}
