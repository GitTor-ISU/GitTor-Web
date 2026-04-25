import { Component, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AvatarsService } from '@core/avatars-service';
import SessionService from '@core/session-service';
import { UserAvatarsService } from '@generated/openapi/services/user-avatars';
import { UsersService } from '@generated/openapi/services/users';
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
import { LucideIconData, Trash2Icon, UploadIcon } from 'lucide-angular';
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
  ],
  templateUrl: './profile-settings.html',
})
export class ProfileSettings implements SettingsFormTab {
  public readonly form = new FormGroup({
    avatar: new FormControl<Blob | null>(null, {
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
  public readonly isInvalid = toSignal(this.form.statusChanges.pipe(map(() => this.form.invalid)), {
    initialValue: this.form.invalid,
  });
  public readonly isPristine = toSignal(this.form.valueChanges.pipe(map(() => this.form.pristine)), {
    initialValue: this.form.pristine,
  });

  protected readonly sessionService = inject(SessionService);
  protected readonly avatarsService = inject(AvatarsService);
  private readonly userAvatarsService = inject(UserAvatarsService);
  protected readonly user = this.sessionService.user;

  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly avatarHelpMessage = signal('');
  protected readonly usernameHelpMessage = createHelpMessageSignal(this.form.controls.username, this.formValue);
  protected readonly firstNameHelpMessage = createHelpMessageSignal(this.form.controls.firstName, this.formValue);
  protected readonly lastNameHelpMessage = createHelpMessageSignal(this.form.controls.lastName, this.formValue);

  protected readonly avatarDisplay = toSignal(
    this.form.controls.avatar.valueChanges.pipe(
      map((file) => this.mapAvatarUrl(file)),
      finalize(() => {
        if (this.avatarObjectUrl) {
          URL.revokeObjectURL(this.avatarObjectUrl);
          this.avatarObjectUrl = null;
        }
      })
    ),
    { initialValue: null }
  );
  protected readonly uploadIcon: LucideIconData = UploadIcon;
  protected readonly trashIcon: LucideIconData = Trash2Icon;

  private avatarObjectUrl: string | null = null;
  private readonly alertDialogService = inject(ZardAlertDialogService);
  private readonly dialogService = inject(ZardDialogService);
  private readonly usersService = inject(UsersService);

  public constructor() {
    effect(() => {
      this.form.addValidators(formDiffValidator(this.form.getRawValue()));
      this.form.controls.username.addValidators(controlMatchValidator(() => this.user()?.username));
      this.form.controls.firstName.addValidators(controlMatchValidator(() => this.user()?.firstname));
      this.form.controls.lastName.addValidators(controlMatchValidator(() => this.user()?.lastname));
      this.form.updateValueAndValidity();
    });
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
        ).then(() => {
          return firstValueFrom(this.sessionService.fetchMe$());
        })
      );
    }

    if (avatar) {
      requests.push(
        firstValueFrom(this.userAvatarsService.updateMyAvatar(avatar)).then(() => {
          this.avatarsService.refetchAvatar();
        })
      );
    }

    if (requests.length > 0) {
      await Promise.all(requests);
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
      this.form.updateValueAndValidity();
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
      zDescription: 'Your account will be deleted.',
      zOkText: 'Continue',
      zOkDestructive: true,
      zCancelText: 'Cancel',
      zOnOk: async () => {
        await firstValueFrom(this.usersService.deleteMe());
        await this.sessionService.logout();
      },
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
      zOnOk: async (instance) => {
        await firstValueFrom(this.usersService.updateMyPassword(instance.form.controls.password.value));
      },
    });
  }

  protected onDeleteAvatar(): void {
    this.alertDialogService.confirm({
      zTitle: 'Are you sure?',
      zDescription: 'Your avatar will be deleted.',
      zOkText: 'Continue',
      zOkDestructive: true,
      zCancelText: 'Cancel',
      zOnOk: async () => {
        await firstValueFrom(this.userAvatarsService.deleteMyAvatar());
        this.avatarsService.refetchAvatar();
        this.form.controls.avatar.reset(null);
      },
    });
  }

  private mapAvatarUrl(file: Blob | null): string | null {
    if (this.avatarObjectUrl) {
      URL.revokeObjectURL(this.avatarObjectUrl);
      this.avatarObjectUrl = null;
    }

    if (!file) {
      return null;
    }

    this.avatarObjectUrl = URL.createObjectURL(file);
    return this.avatarObjectUrl;
  }
}
