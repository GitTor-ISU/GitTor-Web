import { Component, effect, input, signal } from '@angular/core';
import { ZardIconComponent } from '@shared/components/z-icon';
import { ZardTabComponent, ZardTabGroupComponent } from '@shared/components/z-tabs';
import { BookOpenIcon } from 'lucide-angular';

enum DocsEnum {
  installation = 'Installation',
  configuration = 'Configuration',
  usage = 'Usage',
  faq = 'FAQ',
}

/**
 * Getting-started / documentation host. Renders the tab navigation; each tab routes to a
 * child component that owns its own content.
 */
@Component({
  selector: 'app-docs',
  imports: [ZardIconComponent, ZardTabComponent, ZardTabGroupComponent],
  templateUrl: './docs.html',
})
export class Docs {
  public readonly page = input.required<keyof typeof DocsEnum>();

  protected readonly tabLabels = Object.values(DocsEnum);
  protected readonly activeTabIndex = signal(0);
  protected readonly bookIcon = BookOpenIcon;

  public constructor() {
    effect(() => {
      const index = Object.keys(DocsEnum).indexOf(this.page());
      this.activeTabIndex.set(index >= 0 ? index : 0);
    });
  }
}
