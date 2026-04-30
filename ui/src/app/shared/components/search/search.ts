import { Component, effect, ElementRef, input, model, output, signal, untracked, viewChild } from '@angular/core';
import { ArrowRightIcon, SearchIcon } from 'lucide-angular';
import { ZardButtonComponent } from '../z-button';
import { ZardIconComponent } from '../z-icon';
import { ZardInputGroupComponent } from '../z-input-group';
import { ZardInputDirective } from '../z-input/input.directive';
import { ZardPopoverComponent, ZardPopoverDirective } from '../z-popover';

export interface SearchItem {
  label: string;
  category: string;
  display: string;
}

/**
 * Search component.
 */
@Component({
  selector: 'app-search',
  imports: [
    ZardIconComponent,
    ZardButtonComponent,
    ZardPopoverComponent,
    ZardInputGroupComponent,
    ZardInputDirective,
    ZardPopoverDirective,
  ],
  templateUrl: './search.html',
})
export class Search {
  public readonly items = input<SearchItem[]>([]);

  public readonly isSearching = model<boolean>(false);
  public readonly query = model<string>('');

  public readonly itemClicked = output<SearchItem>();
  public readonly searchSubmit = output<string>();

  protected readonly popover = viewChild('popover', { read: ZardPopoverDirective });
  protected readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('searchInput');
  protected readonly triggerable = signal(false);
  protected readonly isVisible = signal(false);

  protected readonly searchIcon = SearchIcon;
  protected readonly arrowRightIcon = ArrowRightIcon;

  public constructor() {
    effect(() => {
      void this.query();
      untracked(() => this.popover()?.hide());
    });

    effect(() => {
      if (this.items().length > 0 && !this.isSearching()) {
        untracked(() => this.popover()?.show());
      } else {
        untracked(() => this.popover()?.hide());
      }
    });
  }

  public focus(): void {
    this.searchInput()?.nativeElement.focus();
  }

  protected onFocus(): void {
    this.triggerable.set(false);
    if (this.items().length > 0) {
      this.popover()?.show();
    }
  }

  protected onBlur(): void {
    this.triggerable.set(true);
  }
}
