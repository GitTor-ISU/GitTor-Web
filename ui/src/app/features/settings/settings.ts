import { Component } from '@angular/core';
import { ZardTabComponent, ZardTabGroupComponent } from '@shared/components/z-tabs';

/**
 * Settings component.
 */
@Component({
  selector: 'app-settings',
  imports: [ZardTabComponent, ZardTabGroupComponent],
  templateUrl: './settings.html',
})
export class Settings {}
