import { NgTemplateOutlet } from '@angular/common';
import { Component, ElementRef, computed, effect, inject, signal, viewChild } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AvatarsService } from '@core/avatars-service';
import SessionService from '@core/session-service';
import { Logo } from '@shared/components/logo/logo';
import { ThemeToggle } from '@shared/components/theme-toggle/theme-toggle';
import { ZardAvatarComponent } from '@shared/components/z-avatar';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardDividerComponent } from '@shared/components/z-divider/divider.component';
import { ZardIconComponent } from '@shared/components/z-icon';
import { ZardInputGroupComponent } from '@shared/components/z-input-group';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
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
  ArrowRightIcon,
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
    ZardInputDirective,
    ZardInputGroupComponent,
    NgTemplateOutlet,
  ],
  templateUrl: './main-layout.html',
})
export class MainLayout {
  protected readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('searchInput');
  protected readonly sessionService = inject(SessionService);
  protected readonly avatarsService = inject(AvatarsService);
  protected readonly user = computed(() => this.sessionService.user());
  protected readonly sidebarCollapsed = signal(true);
  protected logInIcon = LogInIcon;
  protected menuIcon = MenuIcon;
  protected arrowRightIcon = ArrowRightIcon;
  protected searchIcon = SearchIcon;

  protected mainMenuItems: MenuItem[] = [
    { icon: SearchIcon, label: 'Search' },
    { icon: HouseIcon, label: 'Home', route: '/' },
    { icon: InfoIcon, label: 'About', route: '/about' },
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

  protected focusSearch(): void {
    this.sidebarCollapsed.set(false);
    setTimeout(() => this.searchInput()?.nativeElement.focus());
  }

  protected submitSearch(): void {
    const input = this.searchInput()?.nativeElement;
    if (!input || input.value.trim() === '') return;

    console.log(input.value);
    this.sidebarCollapsed.set(true);
    input.value = '';
  }

  protected onCollapsedChange(collapsed: boolean): void {
    this.sidebarCollapsed.set(collapsed);
  }
}
