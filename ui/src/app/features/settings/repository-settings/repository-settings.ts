import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TorrentDto } from '@generated/openapi/models/torrent-dto';
import { ZardAlertDialogService } from '@shared/components/z-alert-dialog/alert-dialog.service';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardComboboxComponent } from '@shared/components/z-combobox';
import { ZardEmptyComponent } from '@shared/components/z-empty';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { ZardLoaderComponent } from '@shared/components/z-loader/loader.component';
import { ZardSegmentedComponent } from '@shared/components/z-segmented';
import { controlMatchValidator } from '@shared/control-match-validator';
import { EmptyToNullDirective } from '@shared/empty-to-null';
import { formDiffValidator } from '@shared/form-diff-validator';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';
import { FolderGit2Icon } from 'lucide-angular';
import { SettingsFormTab, SettingsService } from '../settings-service';

interface Repository {
  id: number;
  name: string;
  about: string;
  torrent: TorrentDto;
  visibility: 'public' | 'private';
}

/**
 * Repository settings page.
 */
@Component({
  selector: 'app-repository-settings',
  imports: [
    ZardButtonComponent,
    ZardEmptyComponent,
    ZardSegmentedComponent,
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
  public form = new FormGroup({
    name: new FormControl<string | null>(null, {
      validators: [Validators.minLength(3), Validators.maxLength(20), Validators.pattern(/^[a-zA-Z0-9_-]*$/)],
      nonNullable: false,
    }),
    about: new FormControl<string | null>(null, {
      validators: [Validators.maxLength(150)],
      nonNullable: false,
    }),
    visibility: new FormControl<string | null>(null, {
      nonNullable: false,
    }),
  });

  protected readonly repositories = signal<Repository[] | undefined>(undefined); // TODO: Replace with API call to fetch repositories
  public readonly showFooter = computed(() => this.repositories() !== undefined && this.repositories()!.length > 0);

  private readonly activatedRoute = inject(ActivatedRoute);
  protected readonly selectedValue = signal<string>(this.activatedRoute.snapshot.queryParamMap.get('id') ?? '');
  protected readonly repository = computed<Repository>(
    () =>
      this.repositories()?.find((repo) => repo?.id.toString() === this.selectedValue()) ??
      this.repositories()?.[0] ??
      ({} as Repository)
  );
  protected readonly visibility = signal<string>('');
  protected readonly repositoryOptions = computed(() =>
    this.repositories()?.map((repository) => ({ label: repository.name, value: repository.id.toString() }))
  );
  protected readonly folderGitIcon = FolderGit2Icon;
  protected readonly visibilityOptions = [
    { label: 'Public', value: 'public' },
    { label: 'Private', value: 'private' },
  ];
  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly nameHelpMessage = createHelpMessageSignal(this.form.controls.name, this.formValue);
  protected readonly aboutHelpMessage = createHelpMessageSignal(this.form.controls.about, this.formValue);

  private readonly router = inject(Router);
  private readonly settingsService = inject(SettingsService);
  private readonly alertDialogService = inject(ZardAlertDialogService);

  public constructor() {
    setTimeout(() => {
      this.repositories.set([
        {
          id: 12,
          name: 'design-system',
          about: 'Reusable UI components, themes, and accessibility primitives for all frontend apps.',
          torrent: {} as TorrentDto,
          visibility: 'public',
        },
        {
          id: 22,
          name: 'ml-experiments',
          about: 'Model training notebooks, evaluation scripts, and experiment tracking for recommendation research.',
          torrent: {} as TorrentDto,
          visibility: 'private',
        },
        {
          id: 32,
          name: 'infra-terraform',
          about: 'Infrastructure-as-code for cloud networking, Kubernetes clusters, and deployment pipelines.',
          torrent: {} as TorrentDto,
          visibility: 'private',
        },
        {
          id: 42,
          name: 'mobile_app_flutter',
          about: 'Cross-platform mobile client with offline sync, push notifications, and biometric login support.',
          torrent: {} as TorrentDto,
          visibility: 'public',
        },
      ]);
      this.onRepositoryChange(this.repository().id?.toString());
    }, 100);

    effect(() => {
      const id = this.repository().id?.toString();
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
    this.visibility.set(this.repository().visibility);
    this.form.addValidators(formDiffValidator(this.form.getRawValue()));
    this.form.controls.name.addValidators(controlMatchValidator(this.repository().name));
    this.form.updateValueAndValidity();
  }

  protected async onRepositoryChange(repo: string): Promise<void> {
    if (await this.settingsService.confirmDiscardChanges(this.form)) {
      this.selectedValue.set(repo);
    }
  }

  protected updateVisibility(visibility: string): void {
    if (visibility !== 'public' && visibility !== 'private') return;

    if (visibility === this.repository().visibility) {
      this.form.controls.visibility.setValue(null);
      this.form.controls.visibility.markAsPristine();
    } else {
      this.form.controls.visibility.setValue(visibility);
      this.form.controls.visibility.markAsDirty();
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
