import { Component } from '@angular/core';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardCardComponent } from '@shared/components/z-card';
import { ZardIconComponent } from '@shared/components/z-icon';
import { CopyIcon } from 'lucide-angular';
import { toast } from 'ngx-sonner';

/**
 * Configuration tab — explains the .gittorconfig file.
 */
@Component({
  selector: 'app-configuration',
  imports: [ZardButtonComponent, ZardCardComponent, ZardIconComponent],
  templateUrl: './config.html',
})
export class Config {
  protected readonly copyIcon = CopyIcon;

  protected readonly configFileExample = `[network]
port=12345
api_url=https://gittor.rent/api
tracker1=https://tracker.moeblog.cn:443/announce
tracker2=https://tr.nyacat.pw:443/announce
tracker3=https://tr.highstar.shop:443/announce
tracker4=https://tracker.gcrenwp.top:443/announce`;

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
