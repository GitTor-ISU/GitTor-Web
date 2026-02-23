import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardDividerComponent } from '@shared/components/z-divider/divider.component';
import { ZardMenuModule } from '@shared/components/z-menu/menu.module';
import {
  BookOpenIcon,
  ChevronDownIcon,
  GitForkIcon,
  LucideAngularModule,
  PlusIcon,
  SearchIcon,
  StarIcon,
} from 'lucide-angular';

/**
 * Repository summary for listing.
 */
interface RepositorySummary {
  name: string;
  description: string;
  visibility: 'Public' | 'Private';
  language: string;
  languageColor: string;
  stars: number;
  forks: number;
  updatedAt: string;
}

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
    ZardMenuModule,
    ZardCardComponent,
    ZardDividerComponent,
  ],
  standalone: true,
  templateUrl: './repository-list.html',
})
export class RepositoryList implements OnInit {
  protected readonly bookIcon = BookOpenIcon;
  protected readonly starIcon = StarIcon;
  protected readonly forkIcon = GitForkIcon;
  protected readonly searchIcon = SearchIcon;
  protected readonly plusIcon = PlusIcon;
  protected readonly chevronDownIcon = ChevronDownIcon;
  protected readonly owner = signal<string>('john-doe');
  protected readonly ownerDisplayName = signal<string>('John Doe');
  protected readonly ownerBio = signal<string>('Full-stack developer passionate about open source');
  protected readonly searchQuery = signal<string>('');
  protected readonly typeFilter = signal<string>('All');
  protected readonly languageFilter = signal<string>('All');
  protected readonly sortBy = signal<string>('Last updated');
  protected readonly repositories = signal<RepositorySummary[]>([
    {
      name: 'awesome-project',
      description: 'A comprehensive solution for modern web development showcasing best practices.',
      visibility: 'Public',
      language: 'TypeScript',
      languageColor: 'bg-blue-500',
      stars: 342,
      forks: 54,
      updatedAt: '2 days ago',
    },
    {
      name: 'cli-tools',
      description: 'Collection of command-line utilities for productivity.',
      visibility: 'Public',
      language: 'Rust',
      languageColor: 'bg-orange-500',
      stars: 128,
      forks: 23,
      updatedAt: '5 days ago',
    },
    {
      name: 'react-components',
      description: 'Reusable React component library with TypeScript support.',
      visibility: 'Public',
      language: 'TypeScript',
      languageColor: 'bg-blue-500',
      stars: 89,
      forks: 12,
      updatedAt: '1 week ago',
    },
    {
      name: 'api-starter',
      description: 'Boilerplate for REST APIs with authentication and database setup.',
      visibility: 'Public',
      language: 'Python',
      languageColor: 'bg-yellow-400',
      stars: 67,
      forks: 31,
      updatedAt: '2 weeks ago',
    },
    {
      name: 'dotfiles',
      description: 'Personal configuration files for development environment.',
      visibility: 'Public',
      language: 'Shell',
      languageColor: 'bg-green-500',
      stars: 45,
      forks: 8,
      updatedAt: '3 weeks ago',
    },
    {
      name: 'private-notes',
      description: 'Personal notes and documentation.',
      visibility: 'Private',
      language: 'Markdown',
      languageColor: 'bg-gray-500',
      stars: 0,
      forks: 0,
      updatedAt: '1 month ago',
    },
  ]);

  protected readonly filteredRepositories = signal<RepositorySummary[]>([]);

  private readonly route = inject(ActivatedRoute);

  public ngOnInit(): void {
    const owner = this.route.snapshot.paramMap.get('owner');

    if (owner) {
      this.owner.set(owner);
    }

    this.updateFilteredRepositories();
  }

  /**
   * Update search query and filter repositories.
   *
   * @param event - Input event
   */
  protected onSearchChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchQuery.set(target.value);
    this.updateFilteredRepositories();
  }

  /**
   * Set type filter.
   *
   * @param type - Type filter value
   */
  protected setTypeFilter(type: string): void {
    this.typeFilter.set(type);
    this.updateFilteredRepositories();
  }

  /**
   * Set language filter.
   *
   * @param language - Language filter value
   */
  protected setLanguageFilter(language: string): void {
    this.languageFilter.set(language);
    this.updateFilteredRepositories();
  }

  /**
   * Set sort order.
   *
   * @param sort - Sort value
   */
  protected setSortBy(sort: string): void {
    this.sortBy.set(sort);
    this.updateFilteredRepositories();
  }

  /**
   * Get unique languages from repositories.
   *
   * @returns Array of unique language names
   */
  protected getLanguages(): string[] {
    const languages = new Set(this.repositories().map((repo) => repo.language));
    return ['All', ...Array.from(languages)];
  }

  /**
   * Update the filtered repositories based on current filters.
   */
  private updateFilteredRepositories(): void {
    let filtered = [...this.repositories()];

    const query = this.searchQuery().toLowerCase();
    if (query) {
      filtered = filtered.filter(
        (repo) => repo.name.toLowerCase().includes(query) || repo.description.toLowerCase().includes(query)
      );
    }

    if (this.typeFilter() !== 'All') {
      filtered = filtered.filter((repo) => repo.visibility === this.typeFilter());
    }

    if (this.languageFilter() !== 'All') {
      filtered = filtered.filter((repo) => repo.language === this.languageFilter());
    }

    switch (this.sortBy()) {
      case 'Name':
        filtered.sort((a, b) => a.name.localeCompare(b.name));
        break;
      case 'Stars':
        filtered.sort((a, b) => b.stars - a.stars);
        break;
      default:
        break;
    }

    this.filteredRepositories.set(filtered);
  }
}
