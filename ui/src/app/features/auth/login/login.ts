import { Component } from '@angular/core';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardFormModule } from '@shared/components/z-form/form.module';

/**
 * Login component.
 */
@Component({
  selector: 'app-login',
  imports: [ZardFormModule, ZardCardComponent],
  templateUrl: './login.html',
})
export class Login {}
