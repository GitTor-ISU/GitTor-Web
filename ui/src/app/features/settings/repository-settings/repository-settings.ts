import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TorrentDto } from '@generated/openapi/models/torrent-dto';
import { UsersService } from '@generated/openapi/services/users';
import { ZardAlertDialogService } from '@shared/components/z-alert-dialog/alert-dialog.service';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardComboboxComponent } from '@shared/components/z-combobox';
import { ZardEmptyComponent } from '@shared/components/z-empty';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { ZardLoaderComponent } from '@shared/components/z-loader/loader.component';
import { controlMatchValidator } from '@shared/control-match-validator';
import { EmptyToNullDirective } from '@shared/empty-to-null';
import { formDiffValidator } from '@shared/form-diff-validator';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';
import { FolderGit2Icon } from 'lucide-angular';
import { map } from 'rxjs';
import { SettingsFormTab, SettingsService } from '../settings-service';

/**
 * Repository settings page.
 */
@Component({
  selector: 'app-repository-settings',
  imports: [
    ZardButtonComponent,
    ZardEmptyComponent,
    ReactiveFormsModule,
    ZardFormModule,
    ZardInputDirective,
    EmptyToNullDirective,
    ZardComboboxComponent,
    ZardLoaderComponent,
    NgTemplateOutlet,
  ],
  templateUrl: './repository-settings.html',
  providers: [SettingsService],
})
export class RepositorySettings implements SettingsFormTab {
  private readonly usersService = inject(UsersService);
  public form = new FormGroup({
    name: new FormControl<string | null>(null, {
      validators: [Validators.minLength(3), Validators.maxLength(20), Validators.pattern(/^[a-zA-Z0-9_-]*$/)],
      nonNullable: false,
    }),
    description: new FormControl<string | null>(null, {
      validators: [Validators.maxLength(150)],
      nonNullable: false,
    }),
  });

  protected readonly repositories = toSignal(
    this.usersService.getMyTorrents().pipe(map((repos) => repos.map((repo) => repo as Required<TorrentDto>)))
  );
  public readonly showFooter = computed(() => this.repositories() !== undefined && this.repositories()!.length > 0);

  private readonly activatedRoute = inject(ActivatedRoute);
  protected readonly selectedValue = signal<string>(this.activatedRoute.snapshot.queryParamMap.get('id') ?? '');
  protected readonly repository = computed(
    () =>
      this.repositories()?.find((repo) => repo.id.toString() === this.selectedValue()) ??
      this.repositories()?.[0] ??
      undefined
  );
  protected readonly visibility = signal<string>('');
  protected readonly repositoryOptions = computed(() =>
    this.repositories()?.map((repo) => ({ label: repo.name, value: repo.id.toString() }))
  );
  protected readonly folderGitIcon = FolderGit2Icon;
  protected readonly visibilityOptions = [
    { label: 'Public', value: 'public' },
    { label: 'Private', value: 'private' },
  ];
  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly nameHelpMessage = createHelpMessageSignal(this.form.controls.name, this.formValue);
  protected readonly descriptionHelpMessage = createHelpMessageSignal(this.form.controls.description, this.formValue);

  private readonly router = inject(Router);
  private readonly settingsService = inject(SettingsService);
  private readonly alertDialogService = inject(ZardAlertDialogService);

  public constructor() {
    effect(() => {
      const id = this.repository()?.id.toString();
      this.router.navigate([], {
        relativeTo: this.activatedRoute,
        queryParams: { id: id },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
      untracked(() => this.onReset());
    });
  }

  public onSubmit(): void {
    console.log('Form submitted with value:', this.form.getRawValue());
  }

  public onReset(): void {
    this.form.reset();
    this.form.addValidators(formDiffValidator(this.form.getRawValue()));
    this.form.controls.name.addValidators(controlMatchValidator(this.repository()?.name));
    this.form.updateValueAndValidity();
  }

  protected async onRepositoryChange(repo: string): Promise<void> {
    if (await this.settingsService.confirmDiscardChanges(this.form)) {
      this.selectedValue.set(repo);
    }
  }

  protected onDeleteRepo(): void {
    this.alertDialogService.confirm({
      zTitle: 'Are you sure?',
      zDescription: 'This action cannot be undone.',
      zOkText: 'Continue',
      zOkDestructive: true,
      zCancelText: 'Cancel',
    });
  }
}
