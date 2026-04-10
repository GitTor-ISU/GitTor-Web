import { Component, effect, inject, WritableSignal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Z_MODAL_DATA } from '@shared/components/z-dialog';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';

/**
 * Add GPG key form.
 */
@Component({
  selector: 'app-add-gpg-key',
  imports: [ZardFormModule, ReactiveFormsModule, ZardInputDirective],
  template: `
    <form class="mb-4 max-w-md space-y-6 text-sm" [formGroup]="form">
      <z-form-field>
        <label z-form-label zRequired>Title</label>
        <z-form-control [helpText]="titleHelpMessage()">
          <input z-input type="text" placeholder="Enter title" formControlName="title" data-test="add-gpg-key-title" />
        </z-form-control>
      </z-form-field>
      <z-form-field>
        <label z-form-label zRequired>Key</label>
        <z-form-control [helpText]="keyHelpMessage()">
          <textarea
            z-input
            type="text"
            placeholder="Begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'"
            formControlName="key"
            data-test="add-gpg-key-text-area"
          ></textarea>
        </z-form-control>
      </z-form-field>
    </form>
  `,
})
export class AddGpgKey {
  public readonly form = new FormGroup({
    title: new FormControl<string>('', {
      validators: [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(20),
        Validators.pattern(/^[a-zA-Z0-9_-]*$/),
      ],
      nonNullable: true,
    }),
    key: new FormControl<string>('', {
      validators: [Validators.required],
      nonNullable: true,
    }),
  });

  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly titleHelpMessage = createHelpMessageSignal(this.form.controls.title, this.formValue);
  protected readonly keyHelpMessage = createHelpMessageSignal(this.form.controls.key, this.formValue);
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
}
