import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '@core/auth';
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
  ],
  templateUrl: './register.html',
})
export class Register {
  protected readonly logoIcon = GitBranchIcon;

  protected registerForm = new FormGroup(
    {
      username: new FormControl('', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)]),
      confirmPassword: new FormControl('', [Validators.required]),
    },
    { validators: passwordMatchValidator }
  );

  private readonly router: Router = inject(Router);
  private readonly auth: Auth = inject(Auth);

  protected onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    console.log('Form submitted:', this.registerForm.value);
    this.auth.setToken('token');
    this.router.navigate(['']);
  }
}
