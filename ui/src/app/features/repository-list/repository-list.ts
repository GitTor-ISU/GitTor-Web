import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardDividerComponent } from '@shared/components/z-divider/divider.component';
import { ZardMenuImports } from '@shared/components/z-menu';
import {
  BookOpenIcon,
  ChevronDownIcon,
  GitForkIcon,
  LucideAngularModule,
  PlusIcon,
  SearchIcon,
  StarIcon,
} from 'lucide-angular';
import { UserDto } from '@generated/openapi/models/user-dto';
import { TorrentDto } from '@generated/openapi/models/torrent-dto';

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
  protected readonly starIcon = StarIcon;
  protected readonly forkIcon = GitForkIcon;
  protected readonly searchIcon = SearchIcon;
  protected readonly plusIcon = PlusIcon;
  protected readonly chevronDownIcon = ChevronDownIcon;
  private readonly route = inject(ActivatedRoute);
  private readonly data = toSignal(this.route.data);

  protected readonly profile = computed(() => this.data()?.['profile'] as UserDto);
  protected readonly displayName = computed(() => {
    const firstname = this.profile()?.firstname ?? '';
    const lastname = this.profile()?.lastname ?? '';
    return `${firstname} ${lastname}`;
  });

  protected readonly torrents = computed(() => {
    const torrents = this.data()?.['torrents'] as TorrentDto[];
    return torrents.map((torrent) => ({
      id: torrent.id ?? 0,
      name: torrent.name ?? '',
      description: torrent.description ?? '',
      repoId: torrent.repoId ?? 'RepoID:DEADBEEF',
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

  }
}
