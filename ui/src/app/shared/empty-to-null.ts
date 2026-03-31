import { Directive, HostListener, inject } from '@angular/core';
import { NgControl } from '@angular/forms';

/**
 * Directive that converts empty string values to null in form controls.
 */
@Directive({ selector: '[appEmptyToNull]' })
export class EmptyToNullDirective {
  private formControl: NgControl = inject(NgControl);

  /**
   * Listens for input events and converts empty string values to null in the associated form control.
   */
  @HostListener('input') public onInput(): void {
    if (typeof this.formControl.value === 'string' && this.formControl.value.trim() === '') {
      this.formControl.reset(null);
    }
  }
}
