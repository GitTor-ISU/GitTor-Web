import { Component, effect, inject, input, signal } from '@angular/core';
import { FormGroup } from '@angular/forms';
import SessionService from '@core/session-service';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardIconComponent } from '@shared/components/z-icon';
import { ZardTabComponent, ZardTabGroupComponent } from '@shared/components/z-tabs';
import { Settings2Icon } from 'lucide-angular';
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
  imports: [ZardTabComponent, ZardTabGroupComponent, ZardButtonComponent, ZardIconComponent],
  templateUrl: './settings.html',
  providers: [SettingsService],
})
export class Settings {
  public readonly page = input.required<keyof typeof SettingsEnum>();

  protected readonly sessionService = inject(SessionService);
  protected readonly tabLabels = Object.values(SettingsEnum);
  protected readonly activeTabIndex = signal(0);
  protected readonly activeTab = signal<SettingsFormTab | null>(null);
  protected readonly settingsIcon = Settings2Icon;

  private readonly settingsService = inject(SettingsService);

  public constructor() {
    effect(() => {
      const index = Object.keys(SettingsEnum).indexOf(this.page());
      this.activeTabIndex.set(index >= 0 ? index : 0);
    });
  }

  public readonly onDeselected = async (): Promise<boolean> => {
    return this.settingsService.confirmDiscardChanges(this.activeTab()?.form);
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
