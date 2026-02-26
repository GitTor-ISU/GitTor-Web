import { Directive, inject, output } from '@angular/core';
import { ZardDialogService } from '@shared/components/z-dialog';
import { Settings } from './settings';

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
    this.dialogService.create({
      zOkText: 'Save changes',
      zTitle: 'Settings',
      zContent: Settings,
      zOnOk: (instance) => {
        console.log('Form submitted:', instance);
      },
      zWidth: '1000px',
    });
  }
}
