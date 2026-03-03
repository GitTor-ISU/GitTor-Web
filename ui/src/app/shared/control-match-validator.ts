import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const controlMisMatchValidator = (compare: string, message?: string): ValidatorFn => {
  return (control: AbstractControl): ValidationErrors | null => {
    const first = control?.value;
    const second = control.parent?.get(compare)?.value ?? compare;

    return first !== second ? { controlMismatch: { message: message ?? 'Value must be the same.' } } : null;
  };
};

export const controlMatchValidator = (compare: string | null, message?: string): ValidatorFn => {
  return (control: AbstractControl): ValidationErrors | null => {
    const first = control?.value;
    const second = compare !== null ? (control.parent?.get(compare)?.value ?? compare) : null;

    return first === second ? { controlMatch: { message: message ?? 'Value must be different.' } } : null;
  };
};
