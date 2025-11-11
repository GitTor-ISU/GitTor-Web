import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '@core/auth';
import { Logo } from '@shared/components/logo/logo';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { LucideAngularModule } from 'lucide-angular';

/**
 * Login component.
 */
@Component({
  selector: 'app-login',
  imports: [
    ZardFormModule,
    ZardCardComponent,
    LucideAngularModule,
    ReactiveFormsModule,
    ZardButtonComponent,
    ZardInputDirective,
    RouterLink,
    Logo,
  ],
  templateUrl: './login.html',
})
export class Login {
  protected loginForm = new FormGroup({
    usernameOrEmail: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required]),
  });

  private readonly router: Router = inject(Router);
  private readonly auth: Auth = inject(Auth);

  protected onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    console.log('Form submitted:', this.loginForm.value);
    this.auth.setToken('token');
    this.router.navigate(['']);
  }
}
