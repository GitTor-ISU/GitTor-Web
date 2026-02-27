import { Component } from '@angular/core';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardSkeletonComponent } from '@shared/components/z-skeleton';

/**
 * Home component
 */
@Component({
  selector: 'app-home',
  imports: [ZardSkeletonComponent, ZardCardComponent],
  templateUrl: './home.html',
})
export class Home {}
