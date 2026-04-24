import { ZardContextMenuDirective } from '@shared/components/z-menu/context-menu.directive';
import { ZardMenuContentDirective } from '@shared/components/z-menu/menu-content.directive';
import { ZardMenuItemDirective } from '@shared/components/z-menu/menu-item.directive';
import { ZardMenuLabelComponent } from '@shared/components/z-menu/menu-label.component';
import { ZardMenuShortcutComponent } from '@shared/components/z-menu/menu-shortcut.component';
import { ZardMenuDirective } from '@shared/components/z-menu/menu.directive';

export const ZardMenuImports = [
  ZardContextMenuDirective,
  ZardMenuContentDirective,
  ZardMenuItemDirective,
  ZardMenuDirective,
  ZardMenuLabelComponent,
  ZardMenuShortcutComponent,
] as const;
