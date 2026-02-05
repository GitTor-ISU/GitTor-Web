import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from '@features/navbar/navbar';
import {
  ZardBreadcrumbComponent,
  ZardBreadcrumbItemComponent,
} from '@shared/components/z-breadcrumb/breadcrumb.component';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardDividerComponent } from '@shared/components/z-divider/divider.component';
import { ZardIcon, ZardIconComponent } from '@shared/components/z-icon';
import {
  ContentComponent,
  LayoutComponent,
  SidebarComponent,
  SidebarGroupComponent,
  SidebarGroupLabelComponent,
} from '@shared/components/z-layout';
import { ZardMenuModule } from '@shared/components/z-menu/menu.module';
import { ZardSkeletonComponent } from '@shared/components/z-skeleton';

interface MenuItem {
  icon: ZardIcon;
  label: string;
  submenu?: { label: string }[];
}

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
    RouterOutlet,
    Navbar,
    SidebarGroupLabelComponent,
    SidebarGroupComponent,
    SidebarComponent,
    LayoutComponent,
    ContentComponent,
  ],
  templateUrl: './main-layout.html',
})
export class MainLayout {
  protected readonly sidebarCollapsed = signal(false);

  protected mainMenuItems: MenuItem[] = [
    { icon: 'house', label: 'Home' },
    { icon: 'inbox', label: 'Inbox' },
  ];

  protected workspaceMenuItems: MenuItem[] = [
    {
      icon: 'folder',
      label: 'Projects',
      submenu: [{ label: 'Design System' }, { label: 'Mobile App' }, { label: 'Website' }],
    },
    { icon: 'calendar', label: 'Calendar' },
    { icon: 'search', label: 'Search' },
  ];

  protected toggleSidebar(): void {
    this.sidebarCollapsed.update((collapsed) => !collapsed);
  }

  /**
   * Updates collapsed state when sidebar emits change.
   *
   * @param collapsed New collapsed state.
   */
  protected onCollapsedChange(collapsed: boolean): void {
    this.sidebarCollapsed.set(collapsed);
  }
}
