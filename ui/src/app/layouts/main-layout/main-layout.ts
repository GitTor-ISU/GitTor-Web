import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, effect, inject, Renderer2, signal, viewChild, viewChildren } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AvatarsService } from '@core/avatars-service';
import SessionService from '@core/session-service';
import { TorrentDto } from '@generated/openapi/models/torrent-dto';
import { UserDto } from '@generated/openapi/models/user-dto';
import { TorrentsService } from '@generated/openapi/services/torrents';
import { UsersService } from '@generated/openapi/services/users';
import { Logo } from '@shared/components/logo/logo';
import { Search, SearchItem } from '@shared/components/search/search';
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
import { ZardPopoverComponent, ZardPopoverDirective } from '@shared/components/z-popover';
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
import { catchError, firstValueFrom, forkJoin, map, of } from 'rxjs';

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
    ZardPopoverDirective,
    ZardPopoverComponent,
    ZardDividerComponent,
    Search,
    RouterLinkActive,
  ],
  templateUrl: './main-layout.html',
})
export class MainLayout {
  protected readonly navPopover = viewChild('navPopover', { read: ZardPopoverDirective });
  protected readonly searches = viewChildren<Search>('search');
  protected readonly sessionService = inject(SessionService);
  protected readonly avatarsService = inject(AvatarsService);
  protected readonly breakpointObserver = inject(BreakpointObserver);
  protected readonly isLargerThanMedium = toSignal(
    this.breakpointObserver.observe(['(min-width: 768px)']).pipe(map((result) => result.matches))
  );
  protected readonly user = computed(() => this.sessionService.user());
  protected readonly sidebarCollapsed = signal(true);
  protected readonly isSearching = signal(false);
  protected readonly isQuerying = signal(false);
  protected readonly queriedUsers = signal<UserDto[]>([]);
  protected readonly queriedTorrents = signal<TorrentDto[]>([]);
  protected readonly items = computed<SearchItem[]>(() => [
    ...this.queriedUsers().map((u) => ({
      label: u.id?.toString() ?? '',
      category: 'Users',
      display: u.username ?? '',
    })),
    ...this.queriedTorrents().map((t) => ({
      label: t.id?.toString() ?? '',
      category: 'Repositories',
      display: `${t.uploaderUsername}/${t.name}`,
    })),
  ]);
  protected logInIcon = LogInIcon;
  protected menuIcon = MenuIcon;
  protected arrowRightIcon = ArrowRightIcon;
  protected searchIcon = SearchIcon;

  protected mainMenuItems: MenuItem[] = [
    { icon: HouseIcon, label: 'Home', route: '/' },
    { icon: InfoIcon, label: 'About', route: '/about' },
  ];

  protected workspaceMenuItems: MenuItem[] = [];

  protected readonly usersService = inject(UsersService);
  private readonly torrentsService = inject(TorrentsService);
  private readonly router = inject(Router);
  private renderer = inject(Renderer2);

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
          route: `/settings`,
        },
      ];
    });

    effect(() => {
      if (this.sidebarCollapsed()) {
        this.clearQueriedItems();
        this.searches()[0]?.reset();
        this.renderer.removeStyle(document.body, 'overflow');
      } else {
        this.renderer.setStyle(document.body, 'overflow', 'hidden');
      }
    });
  }

  protected toggleSidebar(): void {
    this.sidebarCollapsed.update((collapsed) => !collapsed);
  }

  protected focusSearch(): void {
    this.sidebarCollapsed.set(false);
    setTimeout(() => {
      this.searches()[0]?.focus();
    }, 50);
  }

  protected onCollapsedChange(collapsed: boolean): void {
    this.sidebarCollapsed.set(collapsed);
  }

  protected async submitSearch(): Promise<void> {
    if (this.items().length > 0) {
      console.log(this.items()[0]);
      await this.navigateToSearch(this.items()[0]);
      return;
    }

    toast.error('No results found.');
  }

  protected async searchQuery(query: string): Promise<void> {
    if (query.length < 3) {
      this.clearQueriedItems();
      this.isQuerying.set(false);
      return;
    }

    this.isQuerying.set(true);

    const search$ = forkJoin({
      torrent: this.torrentsService.getTorrentByRepoId(query).pipe(
        map((torrent) => torrent),
        catchError(() => of(null))
      ),
      users: this.usersService.searchUsers(query, 0, 5).pipe(catchError(() => of([]))),
      torrents: this.torrentsService.searchTorrents(query, 0, 5).pipe(catchError(() => of([]))),
    }).pipe(
      map(({ torrent, users, torrents }) => ({
        torrent,
        users,
        torrents,
      }))
    );

    await firstValueFrom(search$)
      .then(async (result) => {
        this.queriedTorrents.set(result.torrent ? [result.torrent] : result.torrents);
        this.queriedUsers.set(result.users);
      })
      .finally(() => {
        this.isQuerying.set(false);
        this.focusSearch();
      });
  }

  protected async navigateToSearch(item: SearchItem): Promise<void> {
    if (item.category === 'Users') {
      await this.router.navigate(['/', item.display]);
    } else {
      const torrent = this.queriedTorrents().find((t) => t.id?.toString() === item.label);

      if (!torrent) {
        await this.router.navigate(['/404']);
        return;
      }

      await this.router.navigate(['/', torrent.uploaderUsername], {
        queryParams: { search: torrent.name },
      });
    }

    this.clearQueriedItems();
    this.closeNav();
  }

  protected closeNav(): void {
    this.sidebarCollapsed.set(true);
    this.navPopover()?.hide();
    this.clearQueriedItems();
  }

  protected clearQueriedItems(): void {
    this.queriedTorrents.set([]);
    this.queriedUsers.set([]);
  }
}
