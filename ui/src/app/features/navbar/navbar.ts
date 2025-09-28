import { CommonModule } from '@angular/common';
import { Component, effect, inject, OnInit, signal } from '@angular/core';
import { Auth } from '@core/auth';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { MenuItem } from '@shared/components/z-menu/menu-item.directive';
import { ZardMenuModule } from '@shared/components/z-menu/menu.module';

import { BookAIcon, CircleQuestionMarkIcon, HouseIcon, InfoIcon, LucideAngularModule } from 'lucide-angular';

/**
 * Navbar component
 */
@Component({
  selector: 'app-navbar',
  imports: [ZardMenuModule, ZardButtonComponent, CommonModule, LucideAngularModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar implements OnInit {
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

  protected readonly isVisible = signal(true);
  private auth: Auth = inject(Auth);

  public constructor() {
    effect((onCleanup) => {
      let lastSrollY = window.scrollY;

      const controlNavbar = (): void => {
        const currentScrollY = window.scrollY;
        if (currentScrollY > lastSrollY && currentScrollY > 100) {
          this.isVisible.set(false);
        } else {
          this.isVisible.set(true);
        }

        lastSrollY = currentScrollY;
      };

      window.addEventListener('scroll', controlNavbar);

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
  public refresh(): void {}

  private login(): void {
    this.auth.setToken('token');
    this.refresh();
  }

  private logout(): void {
    this.auth.removeToken();
    this.refresh();
  }
}
