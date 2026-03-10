import { Component } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { SETTINGS_TAB, SettingsFormTab } from '../settings-tab';

/**
 * Repository settings page.
 */
@Component({
  selector: 'app-repository-settings',
  imports: [],
  templateUrl: './repository-settings.html',
  providers: [{ provide: SETTINGS_TAB, useExisting: RepositorySettings }],
})
export class RepositorySettings implements SettingsFormTab {
  public form = null as unknown as FormGroup;

  public onSubmit(): void {}
}
