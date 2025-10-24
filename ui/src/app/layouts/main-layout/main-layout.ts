import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from '@features/navbar/navbar';
import { LayoutModule } from '@shared/components/z-layout/layout.module';

/**
 * Main layout component.
 */
@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, Navbar, LayoutModule],
  templateUrl: './main-layout.html',
})
export class MainLayout {}
