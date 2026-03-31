import { Component, effect, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import SessionService from '@core/session-service';
import ThemeService from '@core/theme-service';
import { NgxGridpatternComponent } from '@omnedia/ngx-gridpattern';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import {
  ArrowRightIcon,
  DownloadIcon,
  GitBranchIcon,
  LockIcon,
  LucideAngularModule,
  NetworkIcon,
  ShieldCheckIcon,
  TerminalIcon,
} from 'lucide-angular';

/**
 * A single feature card definition.
 */
interface Feature {
  icon: any;
  title: string;
  description: string;
}

/**
 * Home page component — landing page for GitTor.
 */
@Component({
  selector: 'app-home',
  imports: [ZardCardComponent, ZardButtonComponent, RouterLink, LucideAngularModule, NgxGridpatternComponent],
  templateUrl: './home.html',
})
export class Home {
  protected readonly sessionService = inject(SessionService);
  protected readonly themeService = inject(ThemeService);
  protected readonly router: Router = inject(Router);

  protected readonly arrowRightIcon = ArrowRightIcon;
  protected readonly terminalIcon = TerminalIcon;
  protected readonly downloadIcon = DownloadIcon;
  protected readonly branchIcon = GitBranchIcon;

  protected readonly features: Feature[] = [
    {
      icon: NetworkIcon,
      title: 'Decentralized',
      description:
        'Repositories are distributed across peers via BitTorrent. No central server means no single point of failure and no vendor lock-in.',
    },
    {
      icon: ShieldCheckIcon,
      title: 'GPG-Verified Commits',
      description:
        'Every commit is cryptographically authenticated using GPG signatures, ensuring committer identity across the peer network.',
    },
    {
      icon: LockIcon,
      title: 'Private by Default',
      description:
        'Your data stays yours. GitTor never shares your repositories with third parties, and self-hosting is always one Docker command away.',
    },
  ];
  public constructor() {
    effect(() => {
      const user = this.sessionService.user();
      if (user) {
        this.router.navigateByUrl(`/${user.username}`);
      }
    });
  }
}
