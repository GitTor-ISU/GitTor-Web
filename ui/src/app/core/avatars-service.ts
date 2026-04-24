import { computed, inject, Injectable } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import SessionService from '@core/session-service';
import { thumbs } from '@dicebear/collection';
import { createAvatar } from '@dicebear/core';
import { UserAvatarsService } from '@generated/openapi/services/user-avatars';
import { catchError, of, startWith, Subject, switchMap } from 'rxjs';

/**
 * Avatars Service.
 */
@Injectable({ providedIn: 'root' })
export class AvatarsService {
  private readonly userAvatarsService = inject(UserAvatarsService);
  private readonly sessionService = inject(SessionService);
  private readonly defaultAvatarSeed = computed(() => this.sessionService.user()?.id?.toString() ?? '0');
  private readonly defaultAvatarUrl = computed(() =>
    createAvatar(thumbs, {
      seed: this.defaultAvatarSeed(),
      scale: 85,
    }).toDataUri()
  );
  private readonly refetchAvatar$ = new Subject<void>();
  private readonly myAvatar = toSignal(
    this.refetchAvatar$.pipe(
      startWith(undefined),
      switchMap(() =>
        this.sessionService.hasToken()
          ? this.userAvatarsService.getMyAvatar().pipe(catchError(() => of(null)))
          : of(null)
      )
    )
  );
  private myAvatarObjectUrl: string | null = null;
  public readonly isDefaultAvatar = computed(() => this.myAvatar() === null);

  public readonly myAvatarUrl = computed(() => {
    const avatar = this.myAvatar();

    if (!avatar) {
      return this.defaultAvatarUrl();
    }

    if (this.myAvatarObjectUrl) {
      URL.revokeObjectURL(this.myAvatarObjectUrl);
      this.myAvatarObjectUrl = null;
    }

    this.myAvatarObjectUrl = URL.createObjectURL(avatar);
    return this.myAvatarObjectUrl;
  });

  public refetchAvatar(): void {
    this.refetchAvatar$.next();
  }
}
