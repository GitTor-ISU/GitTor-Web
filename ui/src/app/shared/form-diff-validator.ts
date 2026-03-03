import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const NO_FORM_CHANGES_ERROR = 'noFormChanges';

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function isEqual(left: unknown, right: unknown): boolean {
  if (Object.is(left, right)) {
    return true;
  }

  if (Array.isArray(left) && Array.isArray(right)) {
    if (left.length !== right.length) {
      return false;
    }

    return left.every((item, index) => isEqual(item, right[index]));
  }

  if (isObject(left) && isObject(right)) {
    const leftKeys = Object.keys(left);
    const rightKeys = Object.keys(right);

    if (leftKeys.length !== rightKeys.length) {
      return false;
    }

    return leftKeys.every((key) => isEqual(left[key], right[key]));
  }

  return false;
}

/**
 * Marks a form invalid when the current value has no differences from the original value.
 *
 * @template T
 * @param originalValue Original form value snapshot to compare against.
 * @returns Angular validator that sets `noFormChanges` when there is no diff.
 */
export const formDiffValidator = <T>(originalValue: T): ValidatorFn => {
  return (control: AbstractControl): ValidationErrors | null => {
    const getRawValue = control.getRawValue;
    const currentValue = typeof getRawValue === 'function' ? getRawValue.call(control) : control.value;

    return isEqual(currentValue, originalValue) ? { [NO_FORM_CHANGES_ERROR]: true } : null;
  };
};

export { NO_FORM_CHANGES_ERROR };
