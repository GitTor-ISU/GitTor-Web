import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { afterRenderEffect, Component, computed, inject, input, signal, viewChildren } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormGroup } from '@angular/forms';
import SessionService from '@core/session-service';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardCardComponent } from '@shared/components/z-card';
import { ZardTabComponent, ZardTabGroupComponent, zPosition } from '@shared/components/z-tabs';
import { map } from 'rxjs';
import { ProfileSettings } from './profile-settings/profile-settings';
import { RepositorySettings } from './repository-settings/repository-settings';
import { SETTINGS_TAB, SettingsService } from './settings-service';

/**
 * Settings component.
 */
@Component({
  selector: 'app-settings',
  imports: [
    ZardTabComponent,
    ZardTabGroupComponent,
    ProfileSettings,
    ZardButtonComponent,
    ZardCardComponent,
    RepositorySettings,
  ],
  templateUrl: './settings.html',
  providers: [SettingsService],
})
export class Settings {
  public readonly currentTab = input<number>(0);

  protected readonly activeTabIndex = signal(0);
  protected readonly sessionService = inject(SessionService);
  protected readonly breakpointObserver = inject(BreakpointObserver);
  protected readonly tabsPosition = toSignal(
    this.breakpointObserver
      .observe([Breakpoints.Medium, Breakpoints.Large, Breakpoints.XLarge])
      .pipe(map(({ matches }): zPosition => (matches ? 'left' : 'top'))),
    { initialValue: 'top' as zPosition }
  );

  protected readonly tabs = viewChildren(SETTINGS_TAB);
  protected readonly activeTab = computed(() => this.tabs()[this.activeTabIndex()]);
  protected readonly submit = computed(() => this.activeTab()?.onSubmit());
  protected readonly activeForm = signal<FormGroup | null>(null);

  private readonly settingsService = inject(SettingsService);

  public constructor() {
    this.activeTabIndex.set(this.currentTab());

    afterRenderEffect(() => {
      this.activeForm.set(this.activeTab()?.form ?? null);
    });
  }

  public readonly onDeselected = async (): Promise<boolean> => {
    return this.settingsService.confirmDiscardChanges(this.activeForm());
  };
}
