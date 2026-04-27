import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import SessionService from '@core/session-service';
import { TorrentsService } from '@generated/openapi/services/torrents';
import { ZardButtonComponent } from '@shared/components/z-button/button.component';
import { ZardDividerComponent } from '@shared/components/z-divider/divider.component';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { EmptyToNullDirective } from '@shared/empty-to-null';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';
import { LucideAngularModule, UploadIcon } from 'lucide-angular';
import { firstValueFrom, map } from 'rxjs';

const TORRENT_NAME_PATTERN = /^[a-zA-Z0-9_-]*$/;
const TORRENT_MIME_TYPE = 'application/x-bittorrent';

/**
 * Repository upload form component.
 */
@Component({
  selector: 'app-repository-upload',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    LucideAngularModule,
    ZardButtonComponent,
    ZardDividerComponent,
    ZardFormModule,
    ZardInputDirective,
    EmptyToNullDirective,
  ],
  standalone: true,
  templateUrl: './repository-upload.html',
})
export class RepositoryUpload {
  protected readonly uploadIcon = UploadIcon;

  public readonly form = new FormGroup({
    name: new FormControl<string | null>(null, {
      validators: [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(255),
        Validators.pattern(TORRENT_NAME_PATTERN),
      ],
      nonNullable: false,
    }),
    description: new FormControl<string | null>(null, {
      validators: [Validators.maxLength(255)],
      nonNullable: false,
    }),
    file: new FormControl<File | null>(null, {
      validators: [Validators.required],
      nonNullable: false,
    }),
  });

  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly nameHelpMessage = createHelpMessageSignal(this.form.controls.name, this.formValue);
  protected readonly descriptionHelpMessage = createHelpMessageSignal(this.form.controls.description, this.formValue);

  protected readonly isInvalid = toSignal(this.form.statusChanges.pipe(map(() => this.form.invalid)), {
    initialValue: this.form.invalid,
  });
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly fileName = computed(() => this.formValue().file?.name ?? '');

  private readonly sessionService = inject(SessionService);
  private readonly torrentsService = inject(TorrentsService);
  private readonly router = inject(Router);

  protected readonly username = computed(() => this.sessionService.user()?.username ?? '');

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const picked = input.files?.[0] ?? null;
    const file = picked ? new File([picked], picked.name, { type: TORRENT_MIME_TYPE }) : null;
    this.form.controls.file.setValue(file);
    this.form.controls.file.markAsDirty();
    this.form.controls.file.markAsTouched();
  }

  protected async onSubmit(): Promise<void> {
    if (this.form.invalid || this.submitting()) {
      return;
    }
    const { name, description, file } = this.form.getRawValue();
    if (!name || !file) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    try {
      await firstValueFrom(
        this.torrentsService.uploadTorrent(
          {
            name,
            description: description ?? undefined,
          },
          file
        )
      );
      const username = this.username();
      if (username) {
        await this.router.navigate([`/${username}`]);
      } else {
        await this.router.navigate(['/']);
      }
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Upload failed.';
      this.errorMessage.set(message);
    } finally {
      this.submitting.set(false);
    }
  }
}
