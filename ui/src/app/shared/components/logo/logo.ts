import { Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import ThemeService from '@core/theme-service';

/**
 * GitTor Logo Component.
 */
@Component({
  selector: 'app-logo',
  imports: [RouterLink],
  standalone: true,
  templateUrl: './logo.html',
})
export class Logo {
  public readonly mode = input<'full' | 'icon'>('icon');
  public readonly theme = input<'system' | 'light' | 'dark'>('system');
  public readonly link = input<boolean>(false);

  protected readonly themeService = inject(ThemeService);

  /**
   * Get the path to the
   *
   * @returns Path to image file.
   */
  protected get src(): string {
    const theme = this.theme() === 'system' ? this.themeService.getCurrentTheme() : this.theme();
    const mode = this.mode();

    switch (mode) {
      case 'icon':
        return theme === 'dark' ? 'gittor_icon_dark.svg' : 'gittor_icon.svg';
      case 'full':
        return theme === 'dark' ? 'gittor_full_dark.svg' : 'gittor_full.svg';
      default:
        return 'gittor_full.svg';
    }
  }
}
