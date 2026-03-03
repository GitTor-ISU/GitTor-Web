import { Directive, inject, output, signal, type WritableSignal } from '@angular/core';
import { ZardDialogService } from '@shared/components/z-dialog';
import { Settings } from './settings';

export interface iDialogData {
  $disabled: WritableSignal<boolean>;
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

  private dialogService = inject(ZardDialogService);

  protected onClick(event: MouseEvent): void {
    this.appSettingsClick.emit(event);
    this.openDialog();
  }

  private openDialog(): void {
    const $disabled = signal(true);

    this.dialogService.create({
      zOkText: 'Save changes',
      zTitle: 'Settings',
      zData: { $disabled } as iDialogData,
      zContent: Settings,
      zOnOk: (instance) => {
        return instance.submit();
      },
      zOkDisabled: $disabled,
      zWidth: '1000px',
    });
  }
}
