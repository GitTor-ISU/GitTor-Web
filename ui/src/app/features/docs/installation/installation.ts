import { Component } from '@angular/core';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardCardComponent } from '@shared/components/z-card';
import { ZardIconComponent } from '@shared/components/z-icon';
import { CopyIcon, DownloadIcon, ExternalLinkIcon } from 'lucide-angular';
import { toast } from 'ngx-sonner';

/**
 * Installation tab — Linux build steps and links to the CLI repo.
 */
@Component({
  selector: 'app-installation',
  imports: [ZardButtonComponent, ZardCardComponent, ZardIconComponent],
  templateUrl: './installation.html',
})
export class Installation {
  protected readonly copyIcon = CopyIcon;
  protected readonly externalLinkIcon = ExternalLinkIcon;

  protected readonly cliRepoUrl = 'https://github.com/GitTor-ISU/GitTor-Cli';

  protected readonly debianDepsCommand = `sudo apt update
sudo apt install -y build-essential \\
    libtorrent-rasterbar-dev \\
    libglib2.0-dev \\
    libgit2-dev \\
    libcurl4-openssl-dev \\
    libjson-glib-dev`;

  protected readonly buildFromSourceCommand = `git clone https://github.com/GitTor-ISU/GitTor-Cli.git
cd GitTor-Cli
make
make install`;

  protected readonly downloadIcon = DownloadIcon;
  protected readonly windowsDownloadUrl = 'https://gittor-isu.github.io/GitTor-Cli/gittor-exe.zip';

  /**
   * Copy text to the clipboard and show a toast on success or failure.
   *
   * @param text Text to copy.
   * @param label Toast message to show on success.
   */
  protected copy(text: string, label = 'Copied'): void {
    navigator.clipboard
      .writeText(text)
      .then(() => toast.success(label))
      .catch(() => toast.error('Could not copy to clipboard'));
  }
}
