import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';

import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardDividerComponent } from '@shared/components/z-divider/divider.component';
import { ZardMenuImports } from '@shared/components/z-menu';
import {
  BookOpenIcon,
  ChevronDownIcon,
  DownloadIcon,
  GitForkIcon,
  LucideAngularModule,
  PlusIcon,
  SearchIcon,
  SettingsIcon,
  TerminalIcon,
} from 'lucide-angular';
import { UserDto } from '@generated/openapi/models/user-dto';
import { TorrentDto } from '@generated/openapi/models/torrent-dto';
import { TorrentsService } from '@generated/openapi/services/torrents';

type User = UserDto & { isCurrentUser: boolean };

/**
 * Repository list component - displays all repositories for an owner.
 */
@Component({
  selector: 'app-repository-list',
  imports: [
    CommonModule,
    RouterLink,
    LucideAngularModule,
    ZardButtonComponent,
    ZardMenuImports,
    ZardCardComponent,
    ZardDividerComponent,
  ],
  standalone: true,
  templateUrl: './repository-list.html',
})
export class RepositoryList {
  protected readonly bookIcon = BookOpenIcon;
  protected readonly forkIcon = GitForkIcon;
  protected readonly searchIcon = SearchIcon;
  protected readonly plusIcon = PlusIcon;
  protected readonly chevronDownIcon = ChevronDownIcon;
  protected readonly downloadIcon = DownloadIcon;
  protected readonly terminalIcon = TerminalIcon;
  protected readonly settingsIcon = SettingsIcon;

  private readonly torrentsService = inject(TorrentsService);
  private readonly route = inject(ActivatedRoute);
  private readonly data = toSignal(this.route.data);

  protected readonly profile = computed(() => this.data()?.['profile'] as User);
  protected readonly displayName = computed(() => {
    const firstname = this.profile()?.firstname ?? '';
    const lastname = this.profile()?.lastname ?? '';
    if (!firstname && !lastname) return undefined;
    return `${firstname} ${lastname}`;
  });

  protected readonly torrents = computed(() => {
    const torrents = this.data()?.['torrents'] as TorrentDto[];
    return torrents.map((torrent) => ({
      id: torrent.id ?? 0,
      name: torrent.name ?? '',
      description: torrent.description ?? '',
      repoId: torrent.repoId ?? 'DEADBEEF',
      updatedAt: torrent.updatedAt ?? '',
    }));
  });

  protected readonly searchQuery = signal<string>('');
  protected readonly sortBy = signal<string>('Last updated');

  protected readonly listedTorrents = computed(() => {
    const filtered = this.torrents().filter((torrent) => {
      const query = this.searchQuery().toLowerCase();
      if (!query) return true;
      return (
        torrent.name.toLowerCase().includes(query) ||
        torrent.description.toLowerCase().includes(query) ||
        torrent.repoId.toLowerCase().includes(query)
      );
    });
    switch (this.sortBy()) {
      case 'Name':
        filtered.sort((a, b) => a.name.localeCompare(b.name));
        break;
      case 'Last updated':
        filtered.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime());
        break;
      default:
        break;
    }
    return filtered;
  });

  /**
   * Update search query and filter repositories.
   *
   * @param event - Input event
   */
  protected onSearchChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchQuery.set(target.value);
  }

  /**
   * Copy leech command to clipboard.
   *
   * @param repoId - Repository ID to leech
   */
  protected copyLeechCmd(repoId: string): void {
    navigator.clipboard.writeText(`gittor leech ${repoId}`);
    toast.success('Leech command copied to clipboard');
  }

  /**
   * Download torrent file for the repository.
   *
   * @param id - Torrent ID
   * @param name - Torrent name
   */
  protected downloadTorrent(id: number, name: string): void {
    firstValueFrom(
      this.torrentsService.getTorrentFile(id, undefined, undefined, { httpHeaderAccept: 'application/x-bittorrent' })
    ).then((file) => {
      const blob = new Blob([file], { type: 'application/x-bittorrent' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = `${name}.torrent`;
      link.click();
      URL.revokeObjectURL(link.href);
    });
  }
}
