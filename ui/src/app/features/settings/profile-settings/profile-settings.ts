import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import SessionService from '@core/session-service';
import { ZardAlertDialogService } from '@shared/components/z-alert-dialog/alert-dialog.service';
import { ZardAvatarComponent } from '@shared/components/z-avatar';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardDialogService } from '@shared/components/z-dialog';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardIconComponent } from '@shared/components/z-icon';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { controlMatchValidator } from '@shared/control-match-validator';
import { EmptyToNullDirective } from '@shared/empty-to-null';
import { formDiffValidator } from '@shared/form-diff-validator';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';
import { LucideIconData, UploadIcon } from 'lucide-angular';
import { map } from 'rxjs';
import { SETTINGS_TAB, SettingsFormTab } from '../settings-tab';
import { ChangePassword } from './change-password';

/**
 * Profile settings page.
 */
@Component({
  selector: 'app-profile',
  imports: [
    ZardFormModule,
    ReactiveFormsModule,
    ZardInputDirective,
    EmptyToNullDirective,
    ZardButtonComponent,
    ZardAvatarComponent,
    ZardIconComponent,
  ],
  templateUrl: './profile-settings.html',
  providers: [{ provide: SETTINGS_TAB, useExisting: ProfileSettings }],
})
export class ProfileSettings implements SettingsFormTab {
  private static readonly DEFAULT_AVATAR_URL =
    'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQDKRD3JXx695EOaMjnRWvaIH0bifN8UqjmBQ&s';

  public readonly form = new FormGroup({
    avatar: new FormControl<File | null>(null, {
      nonNullable: false,
    }),
    username: new FormControl<string | null>(null, {
      validators: [Validators.minLength(3), Validators.maxLength(20), Validators.pattern(/^[a-zA-Z0-9_-]*$/)],
      nonNullable: false,
    }),
    firstName: new FormControl<string | null>(null, {
      validators: [Validators.maxLength(50), Validators.pattern(/^\p{L}(?:\p{L}| |　|-|,|')*$/u)],
      nonNullable: false,
    }),
    lastName: new FormControl<string | null>(null, {
      validators: [Validators.maxLength(50), Validators.pattern(/^\p{L}(?:\p{L}| |　|-|,|')*$/u)],
      nonNullable: false,
    }),
  });

  protected sessionService = inject(SessionService);
  protected readonly user = computed(() => this.sessionService.user());

  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly avatarHelpMessage = signal('');
  protected readonly usernameHelpMessage = createHelpMessageSignal(this.form.controls.username, this.formValue);
  protected readonly firstNameHelpMessage = createHelpMessageSignal(this.form.controls.firstName, this.formValue);
  protected readonly lastNameHelpMessage = createHelpMessageSignal(this.form.controls.lastName, this.formValue);

  protected readonly avatarUrl = toSignal(
    this.form.controls.avatar.valueChanges.pipe(
      map((file) => (file ? URL.createObjectURL(file) : ProfileSettings.DEFAULT_AVATAR_URL))
    ),
    { initialValue: ProfileSettings.DEFAULT_AVATAR_URL }
  );
  protected readonly uploadIcon: LucideIconData = UploadIcon;

  private readonly alertDialogService = inject(ZardAlertDialogService);
  private readonly dialogService = inject(ZardDialogService);

  public constructor() {
    effect(() => {
      this.form.addValidators(formDiffValidator(this.form.getRawValue()));
      this.form.controls.username.addValidators(controlMatchValidator(this.user()?.username ?? ''));
      this.form.controls.firstName.addValidators(controlMatchValidator(this.user()?.firstname ?? ''));
      this.form.controls.lastName.addValidators(controlMatchValidator(this.user()?.lastname ?? ''));
    });
  }

  public onSubmit(): void {
    console.log('Profile form submitted:', this.form.value);
  }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file: File | undefined = input.files?.[0];

    if (file?.type.startsWith('image/')) {
      this.form.controls.avatar.patchValue(file);
      this.form.controls.avatar.markAsDirty();
      this.avatarHelpMessage.set('');
    }

    if (!file?.type.startsWith('image/')) {
      this.avatarHelpMessage.set('File is not an image.');
    }

    input.value = '';
  }

  protected onDeleteAccount(): void {
    this.alertDialogService.confirm({
      zTitle: 'Are you sure?',
      zDescription: 'This action cannot be undone.',
      zOkText: 'Continue',
      zOkDestructive: true,
      zCancelText: 'Cancel',
    });
  }

  protected onChangePassword(): void {
    const disabled = signal(true);

    this.dialogService.create({
      zTitle: 'Change Password',
      zContent: ChangePassword,
      zData: { disabled },
      zOkText: 'Update',
      zOkDisabled: disabled,
      zOnOk: (instance) => {
        console.log('Form submitted:', instance.form.value);
      },
    });
  }
}
