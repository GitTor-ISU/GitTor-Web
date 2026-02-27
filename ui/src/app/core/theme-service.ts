import { Injectable, signal, computed, effect } from '@angular/core';

type Theme = 'light' | 'dark';

/**
 * Theme service using Angular signals for reactive theme state.
 */
@Injectable({
  providedIn: 'root',
})
export default class ThemeService {
  /**
   * Signal representing the current theme. It is initialized based on local storage or system preference.
   */
  public readonly theme = signal<Theme>(this.resolveInitialTheme());

  /**
   * Computed signal that is `true` when the current theme is dark.
   */
  public readonly isDark = computed(() => this.theme() === 'dark');

  private readonly THEME_KEY = 'theme';

  public constructor() {
    effect(() => {
      this.applyTheme(this.theme());
    });
  }

  /**
   * Toggle the theme between light and dark.
   */
  public toggleTheme(): void {
    this.theme.update((current) => (current === 'dark' ? 'light' : 'dark'));
  }

  private resolveInitialTheme(): Theme {
    const savedTheme = localStorage.getItem(this.THEME_KEY);
    const isDark = savedTheme === 'dark' || (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches);
    return isDark ? 'dark' : 'light';
  }

  private applyTheme(theme: Theme): void {
    const html = document.documentElement;
    const isDark = theme === 'dark';

    html.classList.toggle('dark', isDark);
    html.setAttribute('data-theme', theme);
    html.style.colorScheme = theme;
    localStorage.setItem(this.THEME_KEY, theme);
  }
}
