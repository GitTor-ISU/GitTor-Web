import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, effect, inject, signal, viewChild, viewChildren } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AvatarsService } from '@core/avatars-service';
import SessionService from '@core/session-service';
import { TorrentDto } from '@generated/openapi/models/torrent-dto';
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
import { catchError, concat, defaultIfEmpty, EMPTY, firstValueFrom, map } from 'rxjs';

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
  protected readonly torrents = signal<TorrentDto[]>([]);
  protected readonly items = computed<SearchItem[]>(() =>
    this.torrents().map((t) => ({
      label: t.id?.toString() ?? '',
      category: 'Repositories',
      display: `${t.uploaderUsername}/${t.name}`,
    }))
  );
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

  protected readonly usersService = inject(UsersService);
  private readonly torrentsService = inject(TorrentsService);
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

    effect(() => {
      if (this.sidebarCollapsed()) {
        this.torrents.set([]);
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

  protected async submitSearch(query: string): Promise<void> {
    this.isSearching.set(true);
    this.torrents.set([]);

    const search$ = concat(
      this.usersService.getUser(query).pipe(
        map((user) => ({ type: 'user' as const, user })),
        catchError(() => EMPTY)
      ),
      this.torrentsService.getTorrentByRepoId(query).pipe(
        map((torrent) => ({ type: 'repoId' as const, torrent })),
        catchError(() => EMPTY)
      ),
      this.torrentsService.getAllTorrentsByName(query).pipe(
        map((torrents) => ({ type: 'name' as const, torrents })),
        catchError(() => EMPTY)
      )
    ).pipe(defaultIfEmpty(null));

    await firstValueFrom(search$)
      .then(async (result) => {
        if (!result || (result.type === 'name' && result.torrents.length === 0)) {
          toast.error('No results found');
          return;
        }

        if (result.type === 'user') {
          await this.router.navigate(['/', result.user.username]);
        } else if (result.type === 'repoId') {
          await this.router.navigate(['/', result.torrent.uploaderUsername], {
            queryParams: { search: result.torrent.name },
          });
        } else if (result.type === 'name' && result.torrents.length === 1) {
          await this.router.navigate(['/', result.torrents[0].uploaderUsername], {
            queryParams: { search: result.torrents[0].name },
          });
        } else if (result.type === 'name' && result.torrents.length > 1) {
          this.torrents.set(result.torrents);
          return;
        }

        this.closeNav();
      })
      .finally(() => {
        this.isSearching.set(false);
      });
  }

  protected async navigateToRepo(item: SearchItem): Promise<void> {
    const torrent = this.torrents().find((t) => t.id?.toString() === item.label);

    if (!torrent) {
      await this.router.navigate(['/404']);
      return;
    }

    this.torrents.set([]);
    this.closeNav();

    await this.router.navigate(['/', torrent.uploaderUsername], {
      queryParams: { search: torrent.name },
    });
  }

  protected closeNav(): void {
    this.sidebarCollapsed.set(true);
    this.navPopover()?.hide();
    this.torrents.set([]);
  }
}
