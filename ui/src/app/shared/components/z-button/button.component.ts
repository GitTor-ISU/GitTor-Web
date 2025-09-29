import type { ClassValue } from 'clsx';

import {
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  inject,
  input,
  ViewEncapsulation,
} from '@angular/core';

import { mergeClasses, transform } from '@shared/utils/merge-classes';
import { buttonVariants, ZardButtonVariants } from './button.variants';

@Component({
  selector: 'z-button, button[z-button], a[z-button]',
  standalone: true,
  template: `
    @if (zLoading()) {
      <span zType="cached" class="icon-loader-circle animate-spin"></span>
    }

    <ng-content></ng-content>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  host: {
    '[class]': 'classes()',
  },
  exportAs: 'zButton',
})
export class ZardButtonComponent {
  private readonly elementRef = inject(ElementRef);

  readonly zType = input<ZardButtonVariants['zType']>('default');
  readonly zSize = input<ZardButtonVariants['zSize']>('default');
  readonly zShape = input<ZardButtonVariants['zShape']>('default');

  readonly class = input<ClassValue>('');

  readonly zFull = input(false, { transform });
  readonly zLoading = input(false, { transform });

  protected readonly classes = computed(() =>
    mergeClasses(
      buttonVariants({
        zType: this.zType(),
        zSize: this.zSize(),
        zShape: this.zShape(),
        zFull: this.zFull(),
        zLoading: this.zLoading(),
      }),
      this.class()
    )
  );
}
