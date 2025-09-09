import { Injectable } from '@angular/core';

/**
 * Auth service.
 */
@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly TOKEN_KEY = 'authToken';

  /**
   * Set access token.
   *
   * @param {string} token access token
   */
  public setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Get access token.
   *
   * @returns {string | null} access token
   */
  public getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Remove access token.
   */
  public removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  /**
   * Check if access token exists.
   *
   * @returns {boolean} true if access token exists
   */
  public isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
