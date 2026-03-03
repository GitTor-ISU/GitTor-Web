import { Component, effect, inject, signal, viewChildren } from '@angular/core';
import { Z_MODAL_DATA } from '@shared/components/z-dialog';
import { ZardTabComponent, ZardTabGroupComponent } from '@shared/components/z-tabs';
import { Profile } from './profile/profile';
import type { iDialogData } from './settings-directive';
import { SETTINGS_TAB, SettingsTab } from './settings-tab';

/**
 * Settings component.
 */
@Component({
  selector: 'app-settings',
  imports: [ZardTabComponent, ZardTabGroupComponent, Profile],
  templateUrl: './settings.html',
})
export class Settings {
  protected readonly activeTabIndex = signal(0);

  private readonly tabs = viewChildren(SETTINGS_TAB);
  private readonly zData: iDialogData = inject(Z_MODAL_DATA);

  public constructor() {
    effect((onCleanup) => {
      const tab = this.activeTab;
      this.zData.$disabled.set(tab.form.invalid || tab.form.pristine);

      const sub = tab.form.statusChanges.subscribe(() => {
        this.zData.$disabled.set(tab.form.invalid);
      });

      onCleanup(() => sub.unsubscribe());
    });
  }

  /**
   * Get the currently active tab.
   *
   * @returns The active tab.
   */
  public get activeTab(): SettingsTab {
    return this.tabs()[this.activeTabIndex()];
  }

  /**
   * Submit the active tab's form.
   */
  public submit(): void {
    const tab = this.activeTab;
    tab.onSubmit();
  }
}
