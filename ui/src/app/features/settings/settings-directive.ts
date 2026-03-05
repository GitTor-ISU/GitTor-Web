import { Directive, inject, input, output, signal, type WritableSignal } from '@angular/core';
import { ZardDialogService } from '@shared/components/z-dialog';
import { Settings } from './settings';

export interface iSettingsData {
  $disabled: WritableSignal<boolean>;
  $hideFooter: WritableSignal<boolean>;
  currentTab: number;
}

/**
 * Settings Directive.
 */
@Directive({
  selector: '[appSettings]',
  host: {
    '(click)': 'onClick($event)',
  },
})
export class SettingsDirective {
  public readonly appSettingsClick = output<MouseEvent>();
  public readonly currentTab = input<number>(0);
  public readonly width = input<number>(1000);

  private dialogService = inject(ZardDialogService);

  protected onClick(event: MouseEvent): void {
    this.appSettingsClick.emit(event);
    this.openDialog();
  }

  private openDialog(): void {
    const $disabled = signal(true);
    const $hideFooter = signal(false);

    this.dialogService.create({
      zOkText: 'Save changes',
      zTitle: 'Settings',
      zData: { $disabled, $hideFooter, currentTab: this.currentTab() } as iSettingsData,
      zContent: Settings,
      zOnOk: (instance) => {
        return instance.submit();
      },
      zOnCancel: (instance) => instance.onDeselected(),
      zOkDisabled: $disabled,
      zHideFooter: $hideFooter,
      zWidth: `${this.width()}px`,
    });
  }
}
