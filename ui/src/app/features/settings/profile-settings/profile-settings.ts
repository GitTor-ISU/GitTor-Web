import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import SessionService from '@core/session-service';
import { UserAvatarsService } from '@generated/openapi/services/user-avatars';
import { UsersService } from '@generated/openapi/services/users';
import { ZardAlertDialogService } from '@shared/components/z-alert-dialog/alert-dialog.service';
import { ZardAvatarComponent } from '@shared/components/z-avatar';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardDialogService } from '@shared/components/z-dialog';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardIconComponent } from '@shared/components/z-icon';
import { ZardInputGroupComponent } from '@shared/components/z-input-group';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { controlMatchValidator } from '@shared/control-match-validator';
import { EmptyToNullDirective } from '@shared/empty-to-null';
import { formDiffValidator } from '@shared/form-diff-validator';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';
import { LucideIconData, UploadIcon, XIcon } from 'lucide-angular';
import { finalize, firstValueFrom, map } from 'rxjs';
import { SettingsFormTab } from '../settings-service';
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
    ZardInputGroupComponent,
  ],
  templateUrl: './profile-settings.html',
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
  public readonly showFooter = signal(true);

  protected sessionService = inject(SessionService);
  protected readonly user = computed(() => this.sessionService.user());

  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly avatarHelpMessage = signal('');
  protected readonly usernameHelpMessage = createHelpMessageSignal(this.form.controls.username, this.formValue);
  protected readonly firstNameHelpMessage = createHelpMessageSignal(this.form.controls.firstName, this.formValue);
  protected readonly lastNameHelpMessage = createHelpMessageSignal(this.form.controls.lastName, this.formValue);

  protected readonly avatarUrl = toSignal(
    this.form.controls.avatar.valueChanges.pipe(
      map((file) => this.mapAvatarUrl(file)),
      finalize(() => {
        if (this.avatarObjectUrl) {
          URL.revokeObjectURL(this.avatarObjectUrl);
          this.avatarObjectUrl = null;
        }
      })
    ),
    { initialValue: ProfileSettings.DEFAULT_AVATAR_URL }
  );
  protected readonly uploadIcon: LucideIconData = UploadIcon;
  protected readonly xIcon: LucideIconData = XIcon;

  private avatarObjectUrl: string | null = null;
  private readonly alertDialogService = inject(ZardAlertDialogService);
  private readonly dialogService = inject(ZardDialogService);
  private readonly usersService = inject(UsersService);
  private readonly avatarsService = inject(UserAvatarsService);

  public constructor() {
    const effectRef = effect(
      () => {
        this.form.addValidators(formDiffValidator(this.form.getRawValue()));
        this.form.controls.username.addValidators(controlMatchValidator(this.user()?.username));
        this.form.controls.firstName.addValidators(controlMatchValidator(this.user()?.firstname));
        this.form.controls.lastName.addValidators(controlMatchValidator(this.user()?.lastname));
        effectRef.destroy();
      },
      { manualCleanup: true }
    );
  }

  public async onSubmit(): Promise<void> {
    const { avatar, username, firstName, lastName } = this.form.getRawValue();
    const requests: Promise<unknown>[] = [];

    if (username || firstName || lastName) {
      requests.push(
        firstValueFrom(
          this.usersService.updateMe({
            username: username ?? undefined,
            firstname: firstName ?? undefined,
            lastname: lastName ?? undefined,
          })
        )
      );
    }

    if (avatar) {
      requests.push(firstValueFrom(this.avatarsService.updateMyAvatar({ file: avatar })));
    }

    if (requests.length > 0) {
      await Promise.all(requests);
      await firstValueFrom(this.sessionService.fetchMe$());
      this.onReset();
    }
  }

  public onReset(): void {
    this.form.reset();
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

  private mapAvatarUrl(file: File | null): string {
    if (this.avatarObjectUrl) {
      URL.revokeObjectURL(this.avatarObjectUrl);
      this.avatarObjectUrl = null;
    }

    if (!file) {
      return ProfileSettings.DEFAULT_AVATAR_URL;
    }

    this.avatarObjectUrl = URL.createObjectURL(file);
    return this.avatarObjectUrl;
  }
}
