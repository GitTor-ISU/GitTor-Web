import { computed, Signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, ValidationErrors } from '@angular/forms';

const VALIDATION_MESSAGES = {
  invalidEmail: 'Invalid email address.',
  usernamePattern: 'Alphanumeric, hyphen, and underscore characters only.',
  invalidCharacters: 'Invalid character(s).',
  minLength: (requiredLength: number) => `Minimum ${requiredLength} characters.`,
  maxLength: (requiredLength: number) => `Maximum ${requiredLength} characters.`,
} as const;

const USERNAME_PATTERN = /^[a-zA-Z0-9_-]*$/.toString();

export const getErrorMessage = (control: AbstractControl): string => {
  if (!control?.errors) return '';

  const errors: ValidationErrors = control.errors;

  if (errors['required']) return '';

  if (errors['invalidFileType']) return errors['invalidFileType']['message'];

  if (errors['controlMismatch']) return errors['controlMismatch']['message'];
  if (errors['controlMatch']) return errors['controlMatch']['message'];

  if (errors['pattern']?.['requiredPattern'] === USERNAME_PATTERN) return VALIDATION_MESSAGES.usernamePattern;
  if (errors['pattern']) return VALIDATION_MESSAGES.invalidCharacters;

  if (errors['email']) return VALIDATION_MESSAGES.invalidEmail;
  if (errors['minlength']) return VALIDATION_MESSAGES.minLength(errors['minlength'].requiredLength);
  if (errors['maxlength']) return VALIDATION_MESSAGES.maxLength(errors['maxlength'].requiredLength);

  return 'Invalid.';
};

export const createHelpMessageSignal = <T>(control: AbstractControl, formChangeTrigger: Signal<T>): Signal<string> => {
  return computed(() => {
    formChangeTrigger();

    return getErrorMessage(control);
  });
};

export const createFormValueSignal = <T extends AbstractControl>(form: T): Signal<ReturnType<T['getRawValue']>> => {
  return toSignal(form.valueChanges, {
    initialValue: form.getRawValue(),
  });
};
