import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { AvatarModule } from 'primeng/avatar';
import { BadgeModule } from 'primeng/badge';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MenuModule } from 'primeng/menu';
import { Menubar } from 'primeng/menubar';
import { Auth } from '../auth';
import { ThemeSwitcher } from './theme-switcher/theme-switcher';

export interface ThemeState {
  preset?: string;
  primary?: string;
  surface?: string;
  darkTheme?: boolean;
}

/**
 * Navbar component
 */
@Component({
  selector: 'app-navbar',
  imports: [ThemeSwitcher, Menubar, AvatarModule, MenuModule, BadgeModule, InputTextModule, CommonModule, ButtonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar implements OnInit {
  protected userItems: MenuItem[] | undefined;
  protected items: MenuItem[] = [
    {
      label: 'Home',
      icon: 'pi pi-home',
      routerLink: '/',
    },
    {
      label: 'About',
      icon: 'pi pi-info-circle',
      routerLink: '/about',
    },
    {
      label: 'Help',
      icon: 'pi pi-question-circle',
      routerLink: '/help',
    },
    {
      label: 'Contact',
      icon: 'pi pi-address-book',
      routerLink: '/contact',
    },
  ];

  private auth: Auth = inject(Auth);

  /**
   * Initialize.
   */
  public ngOnInit(): void {
    this.refresh();
  }

  /**
   * Refresh the sub-components.
   */
  public refresh(): void {
    const isLoggedIn = this.auth.isLoggedIn();
    this.userItems = [
      { label: 'Profile', icon: 'pi pi-user', routerLink: '/users' },
      { label: 'Settings', icon: 'pi pi-cog', routerLink: '/settings' },
      { separator: true },
      {
        label: 'Login',
        icon: 'pi pi-sign-in',
        command: (): void => this.login(),
        routerLink: '/login',
        visible: !isLoggedIn,
      },
      {
        label: 'Logout',
        icon: 'pi pi-sign-out',
        command: (): void => this.logout(),
        routerLink: '/',
        visible: isLoggedIn,
      },
    ];
  }

  /**
   * Login.
   */
  private login(): void {
    this.auth.setToken('token');
    this.refresh();
  }

  /**
   * Logout.
   */
  private logout(): void {
    this.auth.removeToken();
    this.refresh();
  }
}
