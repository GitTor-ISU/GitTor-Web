import { Component, effect, input, signal } from '@angular/core';
import { ZardIconComponent } from '@shared/components/z-icon';
import { ZardTabComponent, ZardTabGroupComponent } from '@shared/components/z-tabs';
import { BookOpenIcon } from 'lucide-angular';

enum AboutEnum {
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
  selector: 'app-about',
  imports: [ZardIconComponent, ZardTabComponent, ZardTabGroupComponent],
  templateUrl: './about.html',
})
export class About {
  public readonly page = input.required<keyof typeof AboutEnum>();

  protected readonly tabLabels = Object.values(AboutEnum);
  protected readonly activeTabIndex = signal(0);
  protected readonly bookIcon = BookOpenIcon;

  public constructor() {
    effect(() => {
      const index = Object.keys(AboutEnum).indexOf(this.page());
      this.activeTabIndex.set(index >= 0 ? index : 0);
    });
  }
}
