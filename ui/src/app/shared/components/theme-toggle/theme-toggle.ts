import { Component, inject } from '@angular/core';
import ThemeService from '@core/theme-service';
import { MoonIcon, SunIcon } from 'lucide-angular';
import { ZardButtonComponent } from '../z-button/button.component';
import { ZardIconComponent } from '../z-icon';
import { ZardTooltipImports } from '../z-tooltip';

/**
 * Theme toggle component.
 */
@Component({
  selector: 'app-theme-toggle',
  imports: [ZardButtonComponent, ZardTooltipImports, ZardIconComponent],
  templateUrl: './theme-toggle.html',
})
export class ThemeToggle {
  protected readonly themeService = inject(ThemeService);
  protected sunIcon = SunIcon;
  protected moonIcon = MoonIcon;
}
