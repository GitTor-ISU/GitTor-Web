import { Component, effect, ElementRef, input, model, output, signal, untracked, viewChild } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ArrowRightIcon, SearchIcon } from 'lucide-angular';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ZardButtonComponent } from '../z-button';
import { ZardIconComponent } from '../z-icon';
import { ZardInputGroupComponent } from '../z-input-group';
import { ZardInputDirective } from '../z-input/input.directive';
import { ZardLoaderComponent } from '../z-loader/loader.component';
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
    ZardLoaderComponent,
  ],
  templateUrl: './search.html',
})
export class Search {
  public readonly items = input<SearchItem[]>([]);
  public readonly queryDebounce = input<number>(300);

  public readonly isSearching = model<boolean>(false);
  public readonly isQuerying = model<boolean>(false);
  public readonly query = model<string>('');

  public readonly itemClicked = output<SearchItem>();
  public readonly submitInput = output<string>();
  public readonly queryInput = output<string>();

  protected readonly popover = viewChild('popover', { read: ZardPopoverDirective });
  protected readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('searchInput');
  protected readonly triggerable = signal(false);
  protected readonly isVisible = signal(false);

  protected readonly searchIcon = SearchIcon;
  protected readonly arrowRightIcon = ArrowRightIcon;

  private readonly query$ = toObservable(this.query);

  public constructor() {
    effect(() => {
      if (this.items().length > 0 && !this.isSearching()) {
        untracked(() => this.popover()?.show());
      } else {
        untracked(() => this.popover()?.hide());
      }
    });

    effect(() => {
      void this.query();
      this.isQuerying.set(true);
    });

    this.query$
      .pipe(debounceTime(this.queryDebounce()), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((value) => {
        this.queryInput.emit(value);
      });
  }

  public focus(): void {
    this.searchInput()?.nativeElement.focus();
  }

  protected onSubmitInput(): void {
    if (this.isQuerying()) {
      return;
    }
    this.submitInput.emit(this.query());
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
