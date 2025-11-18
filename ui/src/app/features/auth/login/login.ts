import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import SessionService from '@core/session-service';
import { LoginDto } from '@generated/openapi/models/login-dto';
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
    usernameOrEmail: new FormControl<string>('', { validators: [Validators.required], nonNullable: true }),
    password: new FormControl<string>('', { validators: [Validators.required], nonNullable: true }),
  });

  private readonly router: Router = inject(Router);
  private readonly sessionService: SessionService = inject(SessionService);

  protected onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    const { usernameOrEmail, password } = this.loginForm.getRawValue();

    const login: LoginDto = usernameOrEmail.includes('@')
      ? { email: usernameOrEmail, password: password }
      : { username: usernameOrEmail, password: password };

    this.sessionService.login(login).then(() => {
      this.router.navigate(['/']);
    });
  }
}
