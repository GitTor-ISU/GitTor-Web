import { Directive, HostListener, inject } from '@angular/core';
import { NgControl } from '@angular/forms';

/**
 * Directive that converts empty string values to null in form controls.
 */
@Directive({ selector: '[appEmptyToNull]' })
export class EmptyToNullDirective {
  private formControl: NgControl = inject(NgControl);

  /**
   * Listens for keyup events on the host element and converts empty string values to null in the associated form control.
   *
   * @param event The keyboard event triggered on keyup.
   */
  @HostListener('keyup', ['$event']) public onKeyDowns(event: KeyboardEvent): void {
    if (event.key === 'Backspace' && this.formControl.value?.trim() === '') {
      this.formControl.reset(null);
    }
  }
}
