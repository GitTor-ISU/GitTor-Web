import { inject, Injectable, Signal } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ZardAlertDialogService } from '@shared/components/z-alert-dialog/alert-dialog.service';
import { firstValueFrom } from 'rxjs';

export interface SettingsFormTab {
  form: FormGroup;
  showFooter: Signal<boolean>;
  onSubmit(): void;
  onReset(): void;
}

/**
 * Settings helper service.
 */
@Injectable()
export class SettingsService {
  private readonly alertDialogService = inject(ZardAlertDialogService);

  /**
   * Confirms whether the user wants to discard unsaved form changes.
   *
   * @param form The form to evaluate for dirty state.
   * @returns True when navigation/reset should continue; otherwise false.
   */
  public async confirmDiscardChanges(form: FormGroup | null | undefined): Promise<boolean> {
    if (!form || !form.dirty) {
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
  }
}
