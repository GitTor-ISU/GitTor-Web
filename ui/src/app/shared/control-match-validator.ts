import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const VALIDATION_MESSAGES = {
  valueMismatch: 'Values do not match.',
  valueMatch: 'Values must not match.',
} as const;

/**
 * Creates a validator function that checks if the value of the control matches the value of another control or a specified value.
 *
 * @param compare The name of the control to compare against or a specific value to compare with.
 * @param message An optional custom error message to return when the validation fails. If not provided, a default message will be used.
 * @returns A ValidatorFn that can be used in Angular forms to validate that the control's value matches the specified criteria.
 */
export const controlMisMatchValidator = (compare: string, message?: string): ValidatorFn => {
  return (control: AbstractControl): ValidationErrors | null => {
    const first = control?.value;
    const second = control.parent?.get(compare)?.value ?? compare;

    return first !== second ? { controlMismatch: { message: message ?? VALIDATION_MESSAGES.valueMismatch } } : null;
  };
};

/**
 * Creates a validator function that checks if the value of the control don't match the value of another control or a specified value.
 *
 * @param compare The name of the control to compare against or a specific value to compare with.
 * @param message An optional custom error message to return when the validation fails. If not provided, a default message will be used.
 * @returns A ValidatorFn that can be used in Angular forms to validate that the control's value doesn't match the specified criteria.
 */
export const controlMatchValidator = (compare: string | undefined, message?: string): ValidatorFn => {
  return (control: AbstractControl): ValidationErrors | null => {
    if (compare === undefined || compare === null) return null;

    const first = control?.value;
    const second = control.parent?.get(compare)?.value ?? compare;

    return first === second ? { controlMatch: { message: message ?? VALIDATION_MESSAGES.valueMatch } } : null;
  };
};
