import { CommonModule } from '@angular/common';
import { Component, effect, inject, OnInit, signal } from '@angular/core';
import { Auth } from '@core/auth';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { MenuItem } from '@shared/components/z-menu/menu-item.directive';
import { ZardMenuModule } from '@shared/components/z-menu/menu.module';

import { Router, RouterLink } from '@angular/router';
import { ThemeToggle } from '@shared/components/theme-toggle/theme-toggle';
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
export class Navbar implements OnInit {
  protected readonly isLoggedIn = signal(false);
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

  private readonly auth: Auth = inject(Auth);
  private readonly router: Router = inject(Router);

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

        this.isVisible.set(currentScrollY < lastScrollY);

        lastScrollY = currentScrollY;
      };

      window.addEventListener('scroll', controlNavbar, { passive: true });

      onCleanup(() => {
        window.removeEventListener('scroll', controlNavbar);
      });
    });
  }

  public ngOnInit(): void {
    this.refresh();
  }

  /**
   * Refresh the sub-components.
   */
  public refresh(): void {
    this.isLoggedIn.set(this.auth.isLoggedIn());
  }

  protected logout(): void {
    this.auth.removeToken();
    this.router.navigate(['/login']);
  }
}
