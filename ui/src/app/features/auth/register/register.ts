import { Component, inject, OnDestroy, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '@core/auth';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { passwordMatchValidator } from '@shared/password-match-validator';
import { GitBranchIcon, LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';

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
  ],
  templateUrl: './register.html',
})
export class Register implements OnDestroy {
  protected readonly logoIcon = GitBranchIcon;

  protected registerForm = new FormGroup(
    {
      username: new FormControl('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(20),
        Validators.pattern(/^[a-zA-Z0-9_-]*$/),
      ]),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(8)]),
      confirmPassword: new FormControl('', [Validators.required]),
    },
    { validators: passwordMatchValidator }
  );

  protected readonly usernameErrorMessage = signal('');
  protected readonly emailErrorMessage = signal('');
  protected readonly passwordErrorMessage = signal('');
  protected readonly confirmPasswordErrorMessage = signal('');

  private readonly router: Router = inject(Router);
  private readonly auth: Auth = inject(Auth);
  private subscriptions: Subscription[] = [];

  public constructor() {
    const formErrorSubscription = this.registerForm.valueChanges.subscribe(() => {
      this.usernameErrorMessage.set(this._getErrorMessage('username'));
      this.emailErrorMessage.set(this._getErrorMessage('email'));
      this.passwordErrorMessage.set(this._getErrorMessage('password'));
      this.confirmPasswordErrorMessage.set(this._getErrorMessage('confirmPassword'));
    });

    this.subscriptions.push(formErrorSubscription);
  }

  public ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
  }

  protected onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    console.log('Form submitted:', this.registerForm.value);
    this.auth.setToken('token');
    this.router.navigate(['']);
  }

  private _getErrorMessage(controlName: string): string {
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
