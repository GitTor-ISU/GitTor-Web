import { Component, inject, signal } from '@angular/core';
import { ZardAlertDialogService } from '@shared/components/z-alert-dialog/alert-dialog.service';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardCardComponent } from '@shared/components/z-card';
import { ZardDialogService } from '@shared/components/z-dialog';
import { ZardIconComponent } from '@shared/components/z-icon';
import { AddGpgKey } from './add-gpg-key';

type GPGKey = {
  id: number;
  title: string;
  email: string;
  keyId: string;
  subkeys: string[];
  addedOn: Date;
};

const mockGPGKeys: GPGKey[] = [
  {
    id: 1,
    title: 'Work Laptop Key',
    email: 'alex.dev@acme.dev',
    keyId: 'A1B2C3D4E5F67890',
    subkeys: ['9F8E7D6C5B4A3210', '1A2B3C4D5E6F7890'],
    addedOn: new Date('2026-01-14'),
  },
  {
    id: 2,
    title: 'Personal Signing Key',
    email: 'alex.personal@example.com',
    keyId: '0FEDCBA987654321',
    subkeys: ['ABCDEF1234567890'],
    addedOn: new Date('2025-11-03'),
  },
  {
    id: 3,
    title: 'CI Bot Key',
    email: 'ci-bot@gittor.local',
    keyId: '1122334455667788',
    subkeys: ['8877665544332211', '1029384756ABCDEF', '55AA55AA55AA55AA'],
    addedOn: new Date('2026-02-21'),
  },
];

/**
 * GPG settings page placeholder.
 */
@Component({
  selector: 'app-gpg-settings',
  imports: [ZardButtonComponent, ZardIconComponent, ZardCardComponent],
  templateUrl: './gpg-settings.html',
})
export class GpgSettings {
  protected readonly gpgKeys = mockGPGKeys;

  private readonly dialogService = inject(ZardDialogService);
  private readonly alertDialogService = inject(ZardAlertDialogService);

  protected onNew(): void {
    const disabled = signal(true);

    this.dialogService.create({
      zTitle: 'Add a GPG key',
      zContent: AddGpgKey,
      zData: { disabled },
      zOkText: 'Add',
      zOkDisabled: disabled,
      zOnOk: (instance) => {
        console.log('Form submitted:', instance.form.value);
      },
      zWidth: '500px',
    });
  }

  protected onDelete(id: number): void {
    this.alertDialogService.confirm({
      zTitle: 'Are you sure?',
      zDescription: 'This action cannot be undone.',
      zOkText: 'Continue',
      zOkDestructive: true,
      zOnOk: () => {
        console.log(`GPG key deleted: ${id}`);
      },
      zCancelText: 'Cancel',
    });
  }
}
