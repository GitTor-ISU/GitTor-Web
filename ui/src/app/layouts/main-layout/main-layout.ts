import { Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AvatarsService } from '@core/avatars-service';
import SessionService from '@core/session-service';
import { Logo } from '@shared/components/logo/logo';
import { ThemeToggle } from '@shared/components/theme-toggle/theme-toggle';
import { ZardAvatarComponent } from '@shared/components/z-avatar';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardDividerComponent } from '@shared/components/z-divider/divider.component';
import { ZardIconComponent } from '@shared/components/z-icon';
import {
  ContentComponent,
  HeaderComponent,
  LayoutComponent,
  SidebarComponent,
  SidebarGroupComponent,
  SidebarGroupLabelComponent,
} from '@shared/components/z-layout';
import { ZardMenuImports } from '@shared/components/z-menu';
import { ZardTooltipImports } from '@shared/components/z-tooltip';
import {
  FolderIcon,
  HouseIcon,
  InfoIcon,
  LogInIcon,
  LucideIconData,
  MenuIcon,
  SearchIcon,
  SettingsIcon,
} from 'lucide-angular';

interface MenuItem {
  icon: LucideIconData;
  label: string;
  route?: string;
}

/**
 * Main layout component.
 */
@Component({
  selector: 'app-main-layout',
  imports: [
    ZardIconComponent,
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
    RouterLink,
    HeaderComponent,
    ZardMenuImports,
    ZardDividerComponent,
  ],
  templateUrl: './main-layout.html',
})
export class MainLayout {
  protected readonly sessionService = inject(SessionService);
  protected readonly avatarsService = inject(AvatarsService);
  protected readonly user = computed(() => this.sessionService.user());
  protected readonly sidebarCollapsed = signal(true);
  protected logInIcon = LogInIcon;
  protected menuIcon = MenuIcon;

  protected mainMenuItems: MenuItem[] = [
    { icon: HouseIcon, label: 'Home', route: '/' },
    { icon: InfoIcon, label: 'Docs', route: '/docs' },
    { icon: SearchIcon, label: 'Search' },
  ];

  protected workspaceMenuItems: MenuItem[] = [];

  public constructor() {
    effect(() => {
      this.workspaceMenuItems = [
        {
          icon: FolderIcon,
          label: 'Repositories',
          route: `/${this.user()?.username}`,
        },
        {
          icon: SettingsIcon,
          label: 'Settings',
          route: `/settings/profile`,
        },
      ];
    });
  }

  protected toggleSidebar(): void {
    this.sidebarCollapsed.update((collapsed) => !collapsed);
  }

  protected onCollapsedChange(collapsed: boolean): void {
    this.sidebarCollapsed.set(collapsed);
  }
}
