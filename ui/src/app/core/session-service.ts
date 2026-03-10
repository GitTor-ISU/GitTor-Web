import { computed, effect, inject, Injectable, Signal, signal, WritableSignal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationDto } from '@generated/openapi/models/authentication-dto';
import { LoginDto } from '@generated/openapi/models/login-dto';
import { RegisterDto } from '@generated/openapi/models/register-dto';
import { UserDto } from '@generated/openapi/models/user-dto';
import { AuthenticationService } from '@generated/openapi/services/authentication';
import { UsersService } from '@generated/openapi/services/users';
import { catchError, firstValueFrom, Observable, of, tap } from 'rxjs';

/**
 * Auth service.
 */
@Injectable({
  providedIn: 'root',
})
export default class SessionService {
  public readonly accessToken: WritableSignal<AuthenticationDto | null> = signal(null);
  public readonly hasToken: Signal<boolean> = computed(() => !!this.accessToken());
  public readonly user: WritableSignal<UserDto | null> = signal<UserDto | null>(null);

  private readonly TOKEN_KEY = 'accessToken';
  private readonly authService: AuthenticationService = inject(AuthenticationService);
  private readonly usersService = inject(UsersService);
  private readonly router: Router = inject(Router);

  public constructor() {
    // For multi-tab functionality.
    window.addEventListener('storage', (event: StorageEvent) => {
      if (event.key === this.TOKEN_KEY) {
        this.accessToken.set(this.getStoredToken());

        const currentUrl = this.router.url;
        if (currentUrl.startsWith('/login') || currentUrl.startsWith('/register')) {
          this.router.navigateByUrl('/');
        }
      }
    });

    this.accessToken.set(this.getStoredToken());

    // Update local storage on access token changes.
    effect(() => {
      const token = this.accessToken();
      if (token) {
        localStorage.setItem(this.TOKEN_KEY, JSON.stringify(token));
      } else {
        localStorage.removeItem(this.TOKEN_KEY);
      }
    });
  }

  /**
   * Refresh token.
   */
  public async refresh(): Promise<void> {
    try {
      const result = await firstValueFrom(this.authService.refresh(''));
      this.accessToken.set(result);
    } catch (error) {
      this.accessToken.set(null);
      throw error;
    }
  }

  /**
   * Login.
   *
   * @param data Login data
   */
  public async login(data: LoginDto): Promise<void> {
    try {
      const result = await firstValueFrom(this.authService.login(data));
      this.accessToken.set(result);
    } catch (error) {
      this.accessToken.set(null);
      throw error;
    }
  }

  /**
   * Register.
   *
   * @param data Register data
   */
  public async register(data: RegisterDto): Promise<void> {
    try {
      const result = await firstValueFrom(this.authService.register(data));
      this.accessToken.set(result);
    } catch (error) {
      this.accessToken.set(null);
      throw error;
    }
  }

  /**
   * Logout.
   */
  public async logout(): Promise<void> {
    try {
      await firstValueFrom(this.authService.logout(''));
      this.accessToken.set(null);
      this.user.set(null);
      window.location.reload();
    } catch (error) {
      this.accessToken.set(null);
      this.user.set(null);
      throw error;
    }
  }

  /**
   * Fetch current user as an observable stream.
   *
   * @returns User stream with null fallback on failure
   */
  public fetchMe$(): Observable<UserDto | null> {
    return this.usersService.getMe().pipe(
      tap((user) => {
        this.user.set(user);
      }),
      catchError(() => {
        void this.logout();
        return of(null);
      })
    );
  }

  private getStoredToken(): AuthenticationDto | null {
    const tokenString = localStorage.getItem(this.TOKEN_KEY);
    return tokenString ? JSON.parse(tokenString) : null;
  }
}
