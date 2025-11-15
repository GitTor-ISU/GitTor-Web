import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler, Injectable } from '@angular/core';
import { ErrorDto } from '@generated/openapi/models/error-dto';
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
   * @param error Error
   */
  public handleError(error: HttpErrorResponse): void {
    const errorDto: ErrorDto = error.error;
    toast.error(errorDto.message ? errorDto.message : 'Error');
  }
}
