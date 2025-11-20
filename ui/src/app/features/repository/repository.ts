import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardMenuModule } from '@shared/components/z-menu/menu.module';
import {
  BookOpenIcon,
  ChevronDownIcon,
  CodeIcon,
  CopyIcon,
  DownloadIcon,
  EyeIcon,
  FileIcon,
  FolderIcon,
  GitBranchIcon,
  GitForkIcon,
  GitPullRequestIcon,
  HistoryIcon,
  LinkIcon,
  LucideAngularModule,
  MessageSquareIcon,
  SettingsIcon,
  StarIcon,
  TerminalIcon,
  TrendingUpIcon,
  UsersIcon,
} from 'lucide-angular';

/**
 * Repository file item interface.
 */
interface FileItem {
  name: string;
  type: 'folder' | 'file';
  timestamp: string;
}

/**
 * Contributor interface.
 */
interface Contributor {
  name: string;
  initials: string;
  commits: number;
  color: string;
}

/**
 * Language interface.
 */
interface Language {
  name: string;
  percentage: number;
  color: string;
}

/**
 * Repository component - displays a single repository's information.
 */
@Component({
  selector: 'app-repository',
  imports: [CommonModule, LucideAngularModule, ZardButtonComponent, ZardCardComponent, ZardMenuModule],
  standalone: true,
  templateUrl: './repository.html',
})
export class Repository implements OnInit {
  protected readonly eyeIcon = EyeIcon;
  protected readonly starIcon = StarIcon;
  protected readonly forkIcon = GitForkIcon;
  protected readonly codeIcon = CodeIcon;
  protected readonly issuesIcon = MessageSquareIcon;
  protected readonly pullRequestIcon = GitPullRequestIcon;
  protected readonly insightsIcon = TrendingUpIcon;
  protected readonly settingsIcon = SettingsIcon;
  protected readonly branchIcon = GitBranchIcon;
  protected readonly chevronDownIcon = ChevronDownIcon;
  protected readonly folderIcon = FolderIcon;
  protected readonly fileIcon = FileIcon;
  protected readonly historyIcon = HistoryIcon;
  protected readonly copyIcon = CopyIcon;
  protected readonly linkIcon = LinkIcon;
  protected readonly terminalIcon = TerminalIcon;
  protected readonly downloadIcon = DownloadIcon;
  protected readonly usersIcon = UsersIcon;
  protected readonly bookIcon = BookOpenIcon;

  protected readonly owner = signal<string>('john-doe');
  protected readonly repoName = signal<string>('awesome-project');
  protected readonly visibility = signal<'Public' | 'Private'>('Public');
  protected readonly description = signal<string>(
    'A comprehensive solution for modern web development showcasing best practices.'
  );
  protected readonly stars = signal<number>(342);
  protected readonly watching = signal<number>(12);
  protected readonly forks = signal<number>(54);
  protected readonly currentBranch = signal<string>('main');
  protected readonly lastCommitAuthor = signal<string>('john-doe');
  protected readonly lastCommitMessage = signal<string>('Initial commit');
  protected readonly lastCommitTime = signal<string>('2 days ago');
  protected readonly cloneUrl = signal<string>(window.location.href);

  protected readonly activeTab = signal<string>('code');

  protected readonly files = signal<FileItem[]>([
    { name: '.github', type: 'folder', timestamp: '2 days ago' },
    { name: 'src', type: 'folder', timestamp: '5 hours ago' },
    { name: 'public', type: 'folder', timestamp: '1 week ago' },
    { name: 'docs', type: 'folder', timestamp: '3 days ago' },
    { name: '.gitignore', type: 'file', timestamp: '2 weeks ago' },
    { name: 'package.json', type: 'file', timestamp: '1 day ago' },
    { name: 'README.md', type: 'file', timestamp: '3 days ago' },
    { name: 'tsconfig.json', type: 'file', timestamp: '1 week ago' },
    { name: 'LICENSE', type: 'file', timestamp: '1 month ago' },
  ]);

  protected readonly contributors = signal<Contributor[]>([
    { name: 'John Doe', initials: 'JD', commits: 342, color: 'bg-blue-500' },
    { name: 'Jane Smith', initials: 'JS', commits: 287, color: 'bg-green-500' },
    { name: 'Alex Dev', initials: 'AD', commits: 156, color: 'bg-purple-500' },
    { name: 'Sarah Code', initials: 'SC', commits: 98, color: 'bg-yellow-500' },
  ]);

  protected readonly languages = signal<Language[]>([
    { name: 'TypeScript', percentage: 68.3, color: 'bg-blue-500' },
    { name: 'JavaScript', percentage: 21.4, color: 'bg-yellow-400' },
    { name: 'CSS', percentage: 8.2, color: 'bg-purple-500' },
    { name: 'HTML', percentage: 2.1, color: 'bg-red-500' },
  ]);

  protected readonly readmeContent = signal<string>(`
    <h2>Awesome Project</h2>
    <p>A comprehensive solution for modern web development. This project showcases best practices and cutting-edge technologies.</p>
    <h3>Features</h3>
    <ul>
      <li>⚡ Lightning-fast performance</li>
      <li>🎨 Beautiful UI components</li>
      <li>🔒 Secure by design</li>
      <li>📱 Fully responsive</li>
    </ul>
    <h3>Getting Started</h3>
    <pre><code>npm install
npm run dev</code></pre>
  `);

  private readonly title = inject(Title);
  private readonly route = inject(ActivatedRoute);

  public ngOnInit(): void {
    const owner = this.route.snapshot.paramMap.get('owner');
    const name = this.route.snapshot.paramMap.get('name');

    if (owner) {
      this.owner.set(owner);
    }
    if (name) {
      this.repoName.set(name);
    }

    this.title.setTitle(`${this.owner()}/${this.repoName()} - GitTor`);
  }

  /**
   * Set the active tab.
   *
   * @param tab - The tab to activate
   */
  protected setActiveTab(tab: string): void {
    this.activeTab.set(tab);
  }

  /**
   * Copy clone URL to clipboard.
   */
  protected copyCloneUrl(): void {
    navigator.clipboard.writeText(this.cloneUrl());
  }

  /**
   * Download torrent file for the repository.
   */
  protected downloadTorrent(): void {
    const link = document.createElement('a');
    link.href = '/stub.torrent';
    link.download = `${this.repoName()}.torrent`;
    link.click();
  }
}
