import { Pipe, PipeTransform } from '@angular/core';

/**
 * Convert a camelCase string to snake_case.
 */
@Pipe({
  name: 'snakeCase',
})
export class SnakeCasePipe implements PipeTransform {
  /**
   * Transform a camelCase string to snake_case.
   *
   * @param value camelCase string.
   * @returns snake_case string.
   */
  public transform(value: string | null | undefined): string {
    if (value === null || value === undefined) {
      return '';
    }
    return value.replace(/([a-z0-9]|(?=[A-Z]))([A-Z])/g, '$1_$2').toLowerCase();
  }
}
