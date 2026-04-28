import { computed, effect, inject, Injectable } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import SessionService from '@core/session-service';
import { thumbs } from '@dicebear/collection';
import { createAvatar } from '@dicebear/core';
import { UserAvatarsService } from '@generated/openapi/services/user-avatars';
import { catchError, map, Observable, of, startWith, Subject, switchMap } from 'rxjs';

/**
 * Avatars Service.
 */
@Injectable({ providedIn: 'root' })
export class AvatarsService {
  private readonly userAvatarsService = inject(UserAvatarsService);
  private readonly sessionService = inject(SessionService);
  private readonly defaultAvatarSeed = computed(() => this.sessionService.user()?.id?.toString() ?? '0');
  private readonly defaultAvatarUrl = computed(() => this.createDefaultAvatar(this.defaultAvatarSeed()));
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
  private avatarUrlMap = new Map<number, string>();
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

  public constructor() {
    effect(() => {
      void this.sessionService.user();
      this.refetchAvatar();
    });
  }

  public refetchAvatar(): void {
    this.refetchAvatar$.next();
  }

  /**
   * Get user avatar URL or null when invalid.
   *
   * @param userId User id
   * @returns Observable URL or null on error
   */
  public getAvatarUrl$(userId: number): Observable<string | null> {
    if (!userId) return of(null);

    if (this.avatarUrlMap.has(userId)) {
      URL.revokeObjectURL(this.avatarUrlMap.get(userId)!);
      this.avatarUrlMap.delete(userId);
    }

    return this.userAvatarsService.getUserAvatar(userId).pipe(
      map((avatar) => {
        const objectUrl = URL.createObjectURL(avatar);
        this.avatarUrlMap.set(userId, objectUrl);
        return objectUrl;
      }),
      catchError(() => of(this.createDefaultAvatar(userId.toString())))
    );
  }

  private createDefaultAvatar(seed: string): string {
    return createAvatar(thumbs, {
      seed,
      scale: 85,
    }).toDataUri();
  }
}
