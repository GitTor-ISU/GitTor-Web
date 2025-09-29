import { Component, inject, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
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
export class NotFound implements OnInit {
  private title = inject(Title);

  public ngOnInit(): void {
    this.title.setTitle('UI - 404 Not Found');
  }
}
