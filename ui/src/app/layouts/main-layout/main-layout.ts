import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, DestroyRef, effect, ElementRef, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { AvatarsService } from '@core/avatars-service';
import SessionService from '@core/session-service';
import { TorrentsService } from '@generated/openapi/services/torrents';
import { UsersService } from '@generated/openapi/services/users';
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
import { ZardMenuDirective, ZardMenuImports } from '@shared/components/z-menu';
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
import { toast } from 'ngx-sonner';
import { catchError, concat, defaultIfEmpty, EMPTY, filter, firstValueFrom, map } from 'rxjs';

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
  protected readonly navbarMenuTrigger = viewChild(ZardMenuDirective);
  protected readonly sessionService = inject(SessionService);
  protected readonly avatarsService = inject(AvatarsService);
  protected readonly user = computed(() => this.sessionService.user());
  protected readonly sidebarCollapsed = signal(true);
  protected readonly isSearching = signal(false);
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

  private readonly torrentsService = inject(TorrentsService);
  protected readonly usersService = inject(UsersService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);

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

    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.closeNav();
      });
  }

  protected toggleSidebar(): void {
    this.sidebarCollapsed.update((collapsed) => !collapsed);
  }

  protected focusSearch(): void {
    this.sidebarCollapsed.set(false);
    setTimeout(() => this.searchInput()?.nativeElement.focus());
  }

  protected onCollapsedChange(collapsed: boolean): void {
    this.sidebarCollapsed.set(collapsed);
  }

  protected async submitSearch(): Promise<void> {
    const input = this.searchInput()?.nativeElement;
    if (!input || input.value.trim() === '') return;

    const query = input.value.trim();

    this.isSearching.set(true);

    const search$ = concat(
      this.usersService.getUser(query).pipe(
        map((user) => ({ type: 'user' as const, user })),
        catchError(() => EMPTY)
      ),
      this.torrentsService.getTorrentByRepoId(query).pipe(
        map((torrent) => ({ type: 'repoId' as const, torrent })),
        catchError(() => EMPTY)
      ),
      this.torrentsService.getTorrentByName(query).pipe(
        map((torrent) => ({ type: 'name' as const, torrent })),
        catchError(() => EMPTY)
      )
    ).pipe(defaultIfEmpty(null));

    await firstValueFrom(search$)
      .then(async (result) => {
        if (!result) {
          toast.error('No results found');
          return;
        }

        if (result.type === 'user') {
          await this.router.navigate(['/', result.user.username]);
        } else if (result.type === 'repoId' || result.type === 'name') {
          await this.router.navigate(['/', result.torrent.uploaderUsername], {
            queryParams: { search: result.torrent.name },
          });
        }

        this.closeNav();
      })
      .finally(() => {
        this.isSearching.set(false);
      });
  }

  private closeNav(): void {
    this.sidebarCollapsed.set(true);
    this.navbarMenuTrigger()?.close();
  }
}
