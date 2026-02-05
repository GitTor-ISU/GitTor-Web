import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import SessionService from '@core/session-service';
import { UserDto } from '@generated/openapi/models/user-dto';
import { UsersService } from '@generated/openapi/services/users';
import { Logo } from '@shared/components/logo/logo';
import { ThemeToggle } from '@shared/components/theme-toggle/theme-toggle';
import { ZardAlertDialogService } from '@shared/components/z-alert-dialog/alert-dialog.service';
import { ZardAvatarComponent } from '@shared/components/z-avatar';
import { ZardButtonComponent } from '@shared/components/z-button';
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
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';

/**
 * Main layout component.
 */
@Component({
  selector: 'app-main-layout',
  imports: [
    ZardIconComponent,
    ZardMenuModule,
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
    ZardSkeletonComponent,
  ],
  templateUrl: './main-layout.html',
})
export class MainLayout {
  protected readonly sessionService = inject(SessionService);
  protected readonly usersService = inject(UsersService);
  protected readonly sidebarCollapsed = signal(true);
  protected readonly user = signal<UserDto | null>(null);
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

  private readonly alertDialogService: ZardAlertDialogService = inject(ZardAlertDialogService);

  public constructor() {
    effect(async () => {
      this.user.set(await firstValueFrom(this.usersService.getMe()));
    });
  }

  protected toggleSidebar(): void {
    this.sidebarCollapsed.update((collapsed) => !collapsed);
  }

  protected onCollapsedChange(collapsed: boolean): void {
    this.sidebarCollapsed.set(collapsed);
  }

  /**
   * Temporary to demonstrate auth.
   */
  protected async showUser(): Promise<void> {
    if (!this.user()) {
      return;
    }

    this.alertDialogService.confirm({
      zTitle: `Hello ${this.user()?.username}!`,
      zDescription: `Id: ${this.user()?.id}, Email: ${this.user()?.email}`,
      zOkText: 'Delete',
      zCancelText: 'Back',
      zOkDestructive: true,
      zOnOk: () => this.deleteUser(this.user()?.username ?? ''),
    });
  }

  private deleteUser(username: string | undefined): void {
    firstValueFrom(this.usersService.deleteMe()).then(() => {
      toast.success(`'${username}' was deleted`);
      this.sessionService.logout();
    });
  }
}
