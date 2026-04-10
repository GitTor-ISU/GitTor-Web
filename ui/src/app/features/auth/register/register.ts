import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import SessionService from '@core/session-service';
import { RegisterDto } from '@generated/openapi/models/register-dto';
import { Logo } from '@shared/components/logo/logo';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardCardComponent } from '@shared/components/z-card/card.component';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { controlMisMatchValidator } from '@shared/control-match-validator';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';
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

  protected registerForm = new FormGroup({
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
      validators: [Validators.required, controlMisMatchValidator('password', 'Passwords do not match')],
      nonNullable: true,
    }),
  });

  protected readonly formValue = createFormValueSignal(this.registerForm);
  protected readonly usernameHelpMessage = createHelpMessageSignal(this.registerForm.controls.username, this.formValue);
  protected readonly emailHelpMessage = createHelpMessageSignal(this.registerForm.controls.email, this.formValue);
  protected readonly passwordHelpMessage = createHelpMessageSignal(this.registerForm.controls.password, this.formValue);
  protected readonly confirmPasswordHelpMessage = createHelpMessageSignal(
    this.registerForm.controls.confirmPassword,
    this.formValue
  );

  private readonly router: Router = inject(Router);
  private readonly sessionService: SessionService = inject(SessionService);

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
}
