import { Component, effect, inject, WritableSignal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Z_MODAL_DATA } from '@shared/components/z-dialog';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { controlMisMatchValidator } from '@shared/control-match-validator';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';

/**
 * Change password page.
 */
@Component({
  selector: 'app-change-password',
  imports: [ZardFormModule, ReactiveFormsModule, ZardInputDirective],
  template: `
    <form class="mb-4 max-w-md space-y-6 text-sm" [formGroup]="form" (ngSubmit)="onSubmit()">
      <z-form-field>
        <label z-form-label zRequired>New password</label>
        <z-form-control [helpText]="passwordHelpMessage()">
          <input
            z-input
            type="password"
            placeholder="Enter new password"
            formControlName="password"
            data-test="change-password"
          />
        </z-form-control>
      </z-form-field>
      <z-form-field>
        <label z-form-label zRequired>Confirm password</label>
        <z-form-control [helpText]="confirmPasswordHelpMessage()">
          <input
            z-input
            type="password"
            placeholder="Confirm new password"
            formControlName="confirmPassword"
            data-test="confirm-change-password"
          />
        </z-form-control>
      </z-form-field>
    </form>
  `,
})
export class ChangePassword {
  public readonly form = new FormGroup({
    password: new FormControl<string>('', {
      validators: [Validators.required, Validators.minLength(8), Validators.maxLength(72)],
      nonNullable: true,
    }),
    confirmPassword: new FormControl<string>('', {
      validators: [Validators.required, controlMisMatchValidator('password', 'Passwords do not match')],
      nonNullable: true,
    }),
  });

  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly passwordHelpMessage = createHelpMessageSignal(this.form.controls.password, this.formValue);
  protected readonly confirmPasswordHelpMessage = createHelpMessageSignal(
    this.form.controls.confirmPassword,
    this.formValue
  );

  private zData = inject(Z_MODAL_DATA);

  public constructor() {
    effect((cleanup) => {
      const disabled: WritableSignal<boolean> = this.zData['disabled'];

      const sub = this.form.statusChanges.subscribe(() => {
        disabled.set(this.form.invalid);
      });
      cleanup(() => sub.unsubscribe());
    });
  }

  protected onSubmit(): void {
    console.log('Change password form submitted:', this.form.value);
  }
}
