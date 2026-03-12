import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TorrentDto } from '@generated/openapi/models/torrent-dto';
import { ZardButtonComponent } from '@shared/components/z-button';
import { ZardComboboxComponent, ZardComboboxOption } from '@shared/components/z-combobox';
import { ZardEmptyComponent } from '@shared/components/z-empty';
import { ZardFormModule } from '@shared/components/z-form/form.module';
import { ZardInputDirective } from '@shared/components/z-input/input.directive';
import { ZardSegmentedComponent } from '@shared/components/z-segmented';
import { controlMatchValidator } from '@shared/control-match-validator';
import { EmptyToNullDirective } from '@shared/empty-to-null';
import { formDiffValidator } from '@shared/form-diff-validator';
import { createFormValueSignal, createHelpMessageSignal } from '@shared/form-utils';
import { FolderGit2Icon } from 'lucide-angular';
import { SETTINGS_TAB, SettingsFormTab, SettingsService } from '../settings-service';

interface Repository {
  id: string;
  name: string;
  about: string;
  torrent: TorrentDto;
  visibility: 'public' | 'private';
}

const mockRepositories: Repository[] = [
  {
    id: '1',
    name: 'design-system',
    about: 'Reusable UI components, themes, and accessibility primitives for all frontend apps.',
    torrent: {} as TorrentDto,
    visibility: 'public',
  },
  {
    id: '2',
    name: 'ml-experiments',
    about: 'Model training notebooks, evaluation scripts, and experiment tracking for recommendation research.',
    torrent: {} as TorrentDto,
    visibility: 'private',
  },
  {
    id: '3',
    name: 'infra-terraform',
    about: 'Infrastructure-as-code for cloud networking, Kubernetes clusters, and deployment pipelines.',
    torrent: {} as TorrentDto,
    visibility: 'private',
  },
  {
    id: '4',
    name: 'mobile_app_flutter',
    about: 'Cross-platform mobile client with offline sync, push notifications, and biometric login support.',
    torrent: {} as TorrentDto,
    visibility: 'public',
  },
];

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
  ],
  templateUrl: './repository-settings.html',
  providers: [{ provide: SETTINGS_TAB, useExisting: RepositorySettings }, SettingsService],
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

  protected readonly repositories = mockRepositories;
  protected readonly selectedValue = signal<string>('0');
  protected readonly repository = computed(() => this.repositories[Number(this.selectedValue())]);
  protected readonly visibility = signal<string>('');
  protected readonly repositoryOptions = computed(() =>
    this.repositories.map((repository, index) => ({ label: repository.name, value: index.toString() }))
  );
  protected readonly folderGitIcon = FolderGit2Icon;
  protected readonly visibilityOptions = [
    { label: 'Public', value: 'public' },
    { label: 'Private', value: 'private' },
  ];

  protected readonly formValue = createFormValueSignal(this.form);
  protected readonly nameHelpMessage = createHelpMessageSignal(this.form.controls.name, this.formValue);
  protected readonly aboutHelpMessage = createHelpMessageSignal(this.form.controls.about, this.formValue);
  private readonly settingsService = inject(SettingsService);

  public constructor() {
    this.onReset();
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

  protected async onRepositoryChange($event: ZardComboboxOption): Promise<void> {
    if (!(await this.settingsService.confirmDiscardChanges(this.form))) {
      return;
    }
    this.selectedValue.set($event.value);
    this.onReset();
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
}
