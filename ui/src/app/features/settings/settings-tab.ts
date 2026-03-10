import { InjectionToken } from '@angular/core';
import { FormGroup } from '@angular/forms';

export interface SettingsFormTab {
  form: FormGroup;
  onSubmit(): void;
}

export const SETTINGS_TAB = new InjectionToken<SettingsFormTab>('SETTINGS_TAB');
