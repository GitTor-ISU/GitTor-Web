import { Injectable } from '@angular/core';

/**
 * Theme service.
 */
@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly THEME_KEY = 'theme';

  public initTheme(): void {
    const savedTheme = localStorage.getItem(this.THEME_KEY);
    const isDark = savedTheme === 'dark' || (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches);

    this.applyTheme(isDark ? 'dark' : 'light');
  }

  public toggleTheme(): void {
    const currentTheme = this.getCurrentTheme();
    this.applyTheme(currentTheme === 'dark' ? 'light' : 'dark');
  }

  /**
   * Get current theme.
   *
   * @returns {'light' | 'dark'} current theme
   */
  public getCurrentTheme(): 'light' | 'dark' {
    return (localStorage.getItem(this.THEME_KEY) as 'light' | 'dark') || 'light';
  }

  private applyTheme(theme: 'light' | 'dark'): void {
    const html = document.documentElement;
    const isDark = theme === 'dark';

    html.classList.toggle('dark', isDark);
    html.setAttribute('data-theme', theme);
    html.style.colorScheme = theme;
    localStorage.setItem(this.THEME_KEY, theme);
  }
}
