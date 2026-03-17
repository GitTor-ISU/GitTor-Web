import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormGroup } from '@angular/forms';
import SessionService from '@core/session-service';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardCardComponent } from '@shared/components/z-card';
import { ZardTabComponent, ZardTabGroupComponent, zPosition } from '@shared/components/z-tabs';
import { map } from 'rxjs';
import { SettingsFormTab, SettingsService } from './settings-service';

enum SettingsEnum {
  profile = 'Profile',
  repository = 'Repository',
  gpg = 'GPG',
}

/**
 * Settings component.
 */
@Component({
  selector: 'app-settings',
  imports: [ZardTabComponent, ZardTabGroupComponent, ZardButtonComponent, ZardCardComponent],
  templateUrl: './settings.html',
  providers: [SettingsService],
})
export class Settings {
  public readonly page = input.required<keyof typeof SettingsEnum>();

  protected readonly sessionService = inject(SessionService);
  protected readonly breakpointObserver = inject(BreakpointObserver);
  protected readonly tabLabels = Object.values(SettingsEnum);
  protected readonly activeTabIndex = signal(0);
  protected readonly tabsPosition = toSignal(
    this.breakpointObserver
      .observe([Breakpoints.Medium, Breakpoints.Large, Breakpoints.XLarge])
      .pipe(map(({ matches }): zPosition => (matches ? 'left' : 'top'))),
    { initialValue: 'top' as zPosition }
  );
  protected readonly activeTab = signal<SettingsFormTab | null>(null);
  protected readonly activeForm = computed<FormGroup | null>(() => this.activeTab()?.form ?? null);

  private readonly settingsService = inject(SettingsService);

  public constructor() {
    effect(() => {
      const index = Object.keys(SettingsEnum).indexOf(this.page());
      this.activeTabIndex.set(index >= 0 ? index : 0);
    });
  }

  public readonly onDeselected = async (): Promise<boolean> => {
    return this.settingsService.confirmDiscardChanges(this.activeForm());
  };

  protected onTabChange(component: unknown): void {
    if (!this.isSettingsFormTab(component)) {
      this.activeTab.set(null);
      return;
    }

    this.activeTab.set(component);
  }

  private isSettingsFormTab(component: unknown): component is SettingsFormTab {
    if (component == null || typeof component !== 'object') return false;

    const candidate = component as Partial<SettingsFormTab>;
    return (
      typeof candidate.onSubmit === 'function' &&
      typeof candidate.onReset === 'function' &&
      (candidate.form === null || candidate.form instanceof FormGroup)
    );
  }
}
