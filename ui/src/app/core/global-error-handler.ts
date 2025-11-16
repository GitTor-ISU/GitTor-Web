import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler, Injectable } from '@angular/core';
import { toast } from 'ngx-sonner';

/**
 * Global error handler.
 */
@Injectable({
  providedIn: 'root',
})
export class GlobalErrorHandler implements ErrorHandler {
  /**
   * Handle error.
   *
   * @param err Error
   */
  public handleError(err: any): void {
    const message: string = 'An unexpected error occurred';

    if (err instanceof HttpErrorResponse && err.error.message) {
      toast.error(err.error.message);
      console.error('HTTP Error:', err);
      return;
    }

    toast.error(message);
    console.error(err);
  }
}
