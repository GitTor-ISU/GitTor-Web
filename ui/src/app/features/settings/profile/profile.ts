import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import SessionService from '@core/session-service';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { controlMatchValidator } from '@shared/control-match-validator';
import { EmptyToNullDirective } from '@shared/empty-to-null';
import { formDiffValidator } from '@shared/form-diff-validator';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';
import { SETTINGS_TAB, type SettingsTab } from '../settings-tab';

/**
 * Profile settings page.
 */
@Component({
  selector: 'app-profile',
  imports: [ZardFormModule, ReactiveFormsModule, ZardInputDirective, EmptyToNullDirective],
  templateUrl: './profile.html',
  providers: [{ provide: SETTINGS_TAB, useExisting: Profile }],
})
export class Profile implements SettingsTab {
  public readonly form = new FormGroup({
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

  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly usernameErrorMessage = createHelpMessageSignal(this.form.controls.username, this.formValue);
  protected readonly firstNameErrorMessage = createHelpMessageSignal(this.form.controls.firstName, this.formValue);
  protected readonly lastNameErrorMessage = createHelpMessageSignal(this.form.controls.lastName, this.formValue);

  public constructor() {
    this.form.addValidators(formDiffValidator(this.form.getRawValue()));
    this.form.controls.username.addValidators(controlMatchValidator(this.sessionService.user()?.username ?? ''));
    this.form.controls.firstName.addValidators(controlMatchValidator(this.sessionService.user()?.firstname ?? ''));
    this.form.controls.lastName.addValidators(controlMatchValidator(this.sessionService.user()?.lastname ?? ''));
  }

  public onSubmit(): void {
    console.log('Profile form submitted:', this.form.value);
  }
}
