import { Component } from '@angular/core';
import { ZardAlertComponent } from '@shared/components/z-alert/alert.component';

/**
 * 404 not found compnent.
 */
@Component({
  selector: 'app-not-found',
  imports: [ZardAlertComponent],
  templateUrl: './not-found.html',
  styleUrl: './not-found.scss',
})
export class NotFound {}
