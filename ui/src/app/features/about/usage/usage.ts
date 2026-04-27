import { Component, signal } from '@angular/core';
import { ZardCardComponent } from '@shared/components/z-card';
import { ZardIconComponent } from '@shared/components/z-icon';
import { TerminalIcon } from 'lucide-angular';

interface CommandReference {
  name: string;
  syntax: string;
  description: string;
  example?: string;
}

/**
 * Usage tab — typical workflow, full command reference, and identifier cheat sheet.
 */
@Component({
  selector: 'app-usage',
  imports: [ZardCardComponent, ZardIconComponent],
  templateUrl: './usage.html',
})
export class Usage {
  protected readonly terminalIcon = TerminalIcon;

  protected readonly commands = signal<CommandReference[]>([
    {
      name: 'gittor init',
      syntax: 'gittor init [folder]',
      description:
        'Initialize a new GitTor repository. The optional folder argument creates and initializes a directory of that name.',
      example: 'gittor init project',
    },
    {
      name: 'gittor login',
      syntax: 'gittor login',
      description:
        'Authenticate against the configured API host. Prompts for username (or email) and password. Session tokens expire regularly, so expect to run this daily.',
    },
    {
      name: 'gittor seed',
      syntax: 'gittor seed',
      description:
        'Begin seeding the current repository. On first seed, you will be prompted for a name and description. Outputs the repository ID, torrent file path, and magnet link.',
    },
    {
      name: 'gittor leech',
      syntax: 'gittor leech <repo-id | magnet | torrent> [folder]',
      description:
        'Download a repository from the swarm. Once a repo is leeched, running gittor leech inside it with no arguments fetches the latest state automatically.',
      example: 'gittor leech 26671ac0e1a590bba36f97d7ac9c29e51382254f project',
    },
    {
      name: 'gittor service',
      syntax: 'gittor service <status | start | stop | restart>',
      description:
        'Manage the background seeder service. Best practice is to restart after any change to your .gittorconfig file.',
      example: 'gittor service restart',
    },
    {
      name: 'gittor --help',
      syntax: 'gittor --help',
      description: 'List all subcommands, or pass --help to any subcommand for detailed flags and options.',
    },
  ]);
}
