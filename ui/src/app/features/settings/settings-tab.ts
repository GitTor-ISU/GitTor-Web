import { InjectionToken } from '@angular/core';
import { FormGroup } from '@angular/forms';

export interface SettingsTab {
  form: FormGroup;
  onSubmit(): void;
}

export const SETTINGS_TAB = new InjectionToken<SettingsTab>('SETTINGS_TAB');
