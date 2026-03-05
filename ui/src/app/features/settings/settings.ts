import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Component, effect, inject, signal, viewChildren } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ZardAlertDialogService } from '@shared/components/z-alert-dialog/alert-dialog.service';
import { Z_MODAL_DATA } from '@shared/components/z-dialog';
import { ZardTabComponent, ZardTabGroupComponent, zPosition } from '@shared/components/z-tabs';
import { firstValueFrom, map } from 'rxjs';
import { ProfileSettings } from './profile-settings/profile-settings';
import type { iSettingsData } from './settings-directive';
import { SETTINGS_TAB, SettingsTab } from './settings-tab';

/**
 * Settings component.
 */
@Component({
  selector: 'app-settings',
  imports: [ZardTabComponent, ZardTabGroupComponent, ProfileSettings],
  templateUrl: './settings.html',
})
export class Settings {
  protected readonly activeTabIndex = signal(0);
  protected readonly zData: iSettingsData = inject(Z_MODAL_DATA);

  protected readonly breakpointObserver = inject(BreakpointObserver);
  protected readonly tabsPosition = toSignal(
    this.breakpointObserver
      .observe([Breakpoints.Medium, Breakpoints.Large, Breakpoints.XLarge])
      .pipe(map(({ matches }): zPosition => (matches ? 'left' : 'top'))),
    { initialValue: 'top' as zPosition }
  );

  private readonly tabs = viewChildren(SETTINGS_TAB);
  private readonly alertDialogService = inject(ZardAlertDialogService);

  public constructor() {
    this.activeTabIndex.set(this.zData.currentTab);

    effect((onCleanup) => {
      const tab = this.activeTab;

      if (!tab) {
        this.zData.$hideFooter.set(true);
        return;
      }

      this.zData.$hideFooter.set(false);
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
  public get activeTab(): SettingsTab | undefined {
    return this.tabs()[this.activeTabIndex()];
  }

  /**
   * Submit the active tab's form.
   */
  public submit(): void {
    const tab = this.activeTab;
    tab?.onSubmit();
  }

  public readonly onDeselected = async (): Promise<boolean> => {
    if (!this.activeTab?.form.dirty) {
      return true;
    }

    const alertRef = this.alertDialogService.confirm({
      zTitle: 'Unsaved changes',
      zDescription: 'You have unsaved changes. Are you sure you want to leave?',
      zOkText: 'Continue',
      zCancelText: 'Cancel',
      zOnOk: () => ({ confirmed: true }),
    });

    const result = await firstValueFrom(alertRef.afterClosed());
    return result !== undefined;
  };
}
