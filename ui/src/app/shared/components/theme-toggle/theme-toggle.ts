import { Component, inject } from '@angular/core';
import { ThemeService } from '@core/theme';
import { LucideAngularModule, MoonIcon, SunIcon } from 'lucide-angular';
import { ZardButtonComponent } from '../z-button/button.component';

/**
 * Theme toggle component.
 */
@Component({
  selector: 'app-theme-toggle',
  imports: [LucideAngularModule, ZardButtonComponent],
  templateUrl: './theme-toggle.html',
})
export class ThemeToggle {
  protected readonly themeService = inject(ThemeService);
  protected sunIcon = SunIcon;
  protected moonIcon = MoonIcon;
}
