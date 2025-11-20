import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import SessionService from '@core/session-service';
import { RegisterDto } from '@generated/openapi/models/register-dto';
import { Logo } from '@shared/components/logo/logo';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { passwordMatchValidator } from '@shared/password-match-validator';
import { GitBranchIcon, LucideAngularModule } from 'lucide-angular';

/**
 * Register component.
 */
@Component({
  selector: 'app-register',
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
  templateUrl: './register.html',
})
export class Register {
  protected readonly logoIcon = GitBranchIcon;

  protected registerForm = new FormGroup(
    {
      username: new FormControl<string>('', {
        validators: [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(20),
          Validators.pattern(/^[a-zA-Z0-9_-]*$/),
        ],
        nonNullable: true,
      }),
      email: new FormControl<string>('', {
        validators: [Validators.required, Validators.maxLength(255), Validators.email],
        nonNullable: true,
      }),
      password: new FormControl<string>('', {
        validators: [Validators.required, Validators.minLength(8), Validators.maxLength(72)],
        nonNullable: true,
      }),
      confirmPassword: new FormControl<string>('', {
        validators: [Validators.required],
        nonNullable: true,
      }),
    },
    { validators: passwordMatchValidator }
  );

  protected readonly usernameErrorMessage = signal('');
  protected readonly emailErrorMessage = signal('');
  protected readonly passwordErrorMessage = signal('');
  protected readonly confirmPasswordErrorMessage = signal('');

  private readonly router: Router = inject(Router);
  private readonly sessionService: SessionService = inject(SessionService);

  public constructor() {
    this.registerForm.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => {
      this.usernameErrorMessage.set(this.getErrorMessage('username'));
      this.emailErrorMessage.set(this.getErrorMessage('email'));
      this.passwordErrorMessage.set(this.getErrorMessage('password'));
      this.confirmPasswordErrorMessage.set(this.getErrorMessage('confirmPassword'));
    });
  }

  protected onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    const { username, password, email } = this.registerForm.getRawValue();
    const register: RegisterDto = { username, password, email };

    this.sessionService.register(register).then(() => {
      this.router.navigate(['/']);
    });
  }

  private getErrorMessage(controlName: string): string {
    if (controlName === 'confirmPassword' && this.registerForm.hasError('passwordMismatch')) {
      return 'Passwords do not match.';
    }

    const control = this.registerForm.get(controlName);
    if (!control?.errors) return '';

    if (control.errors['email']) return 'Invalid email address.';
    if (control.errors['minlength']) return `Minimum ${control.errors['minlength'].requiredLength} characters.`;
    if (control.errors['maxlength']) return `Maximum ${control.errors['maxlength'].requiredLength} characters.`;
    if (control.errors['pattern'] && controlName === 'username') {
      return `Alphanumeric, hyphen, and underscore characters only.`;
    }

    return '';
  }
}
