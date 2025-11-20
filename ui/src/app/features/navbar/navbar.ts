import { CommonModule } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import SessionService from '@core/session-service';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { MenuItem } from '@shared/components/z-menu/menu-item.directive';
import { ZardMenuModule } from '@shared/components/z-menu/menu.module';

import { RouterLink } from '@angular/router';
import { UserDto } from '@generated/openapi/models/user-dto';
import { UsersService } from '@generated/openapi/services/users';
import { ThemeToggle } from '@shared/components/theme-toggle/theme-toggle';
import { ZardAlertDialogService } from '@shared/components/z-alert-dialog/alert-dialog.service';
import { ZardDividerComponent } from '@shared/components/z-divider/divider.component';
import {
  BookAIcon,
  CircleQuestionMarkIcon,
  HouseIcon,
  InfoIcon,
  LucideAngularModule,
  MenuIcon,
  UserCogIcon,
} from 'lucide-angular';
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';

/**
 * Navbar component
 */
@Component({
  selector: 'app-navbar',
  imports: [
    ZardMenuModule,
    ZardButtonComponent,
    CommonModule,
    LucideAngularModule,
    ZardDividerComponent,
    RouterLink,
    ThemeToggle,
  ],
  templateUrl: './navbar.html',
})
export class Navbar {
  private static readonly DEBOUNCE_MARGIN: number = 10;
  protected readonly sessionService: SessionService = inject(SessionService);
  protected readonly isVisible = signal(true);

  protected userIcon = UserCogIcon;
  protected menuIcon = MenuIcon;
  protected items: MenuItem[] = [
    {
      label: 'Home',
      icon: HouseIcon,
      route: '/',
    },
    {
      label: 'About',
      icon: InfoIcon,
      route: '/about',
    },
    {
      label: 'Help',
      icon: CircleQuestionMarkIcon,
      route: '/help',
    },
    {
      label: 'Contact',
      icon: BookAIcon,
      route: '/contact',
    },
  ];

  private readonly usersService: UsersService = inject(UsersService);
  private readonly alertDialogService: ZardAlertDialogService = inject(ZardAlertDialogService);

  public constructor() {
    effect((onCleanup) => {
      let lastScrollY = window.scrollY;
      let initial = true;

      this.isVisible.set(true);

      const controlNavbar = (): void => {
        const currentScrollY = window.scrollY;

        if (initial) {
          initial = false;
          lastScrollY = currentScrollY;
          return;
        }

        if (currentScrollY < Navbar.DEBOUNCE_MARGIN) {
          // Top (show)
          this.isVisible.set(true);
        } else if (window.innerHeight + currentScrollY >= document.body.offsetHeight - Navbar.DEBOUNCE_MARGIN) {
          // Bottom (hide)
          this.isVisible.set(false);
        } else {
          // Scroll down (hide)
          this.isVisible.set(currentScrollY < lastScrollY);
        }

        lastScrollY = currentScrollY;
      };

      window.addEventListener('scroll', controlNavbar, { passive: true });

      onCleanup(() => {
        window.removeEventListener('scroll', controlNavbar);
      });
    });
  }

  /**
   * Temporary to demonstrate auth.
   */
  protected async showUser(): Promise<void> {
    const user: UserDto = await firstValueFrom(this.usersService.getMe());

    this.alertDialogService.confirm({
      zTitle: `Hello ${user.username}!`,
      zDescription: `Id: ${user.id}, Email: ${user.email}`,
      zOkText: 'Delete',
      zCancelText: 'Back',
      zOkDestructive: true,
      zOnOk: () => this.deleteUser(user.username),
    });
  }

  private deleteUser(username: string | undefined): void {
    firstValueFrom(this.usersService.deleteMe()).then(() => {
      toast.success(`'${username}' was deleted`);
      this.sessionService.logout();
    });
  }
}
