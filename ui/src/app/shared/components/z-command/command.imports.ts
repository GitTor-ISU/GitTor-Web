import { ZardCommandDividerComponent } from '@shared/components/z-command/command-divider.component';
import { ZardCommandEmptyComponent } from '@shared/components/z-command/command-empty.component';
import { ZardCommandInputComponent } from '@shared/components/z-command/command-input.component';
import { ZardCommandListComponent } from '@shared/components/z-command/command-list.component';
import { ZardCommandOptionGroupComponent } from '@shared/components/z-command/command-option-group.component';
import { ZardCommandOptionComponent } from '@shared/components/z-command/command-option.component';
import { ZardCommandComponent } from '@shared/components/z-command/command.component';

export const ZardCommandImports = [
  ZardCommandComponent,
  ZardCommandInputComponent,
  ZardCommandListComponent,
  ZardCommandEmptyComponent,
  ZardCommandOptionComponent,
  ZardCommandOptionGroupComponent,
  ZardCommandDividerComponent,
] as const;
