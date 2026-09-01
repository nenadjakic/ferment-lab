import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { ToastService } from '../../../shared/toast.service';
import { ErrorInfo } from '../api/models';

/**
 * Surfaces API failures as toasts, using the `ErrorInfo` body the backend returns.
 *
 * The error is re-thrown so components can still react (for example keeping a form open), but they
 * never have to build their own message.
 */
export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const toastService = inject(ToastService);

  return next(request).pipe(
    catchError((response: unknown) => {
      toastService.error(describe(response));
      return throwError(() => response);
    })
  );
};

function describe(response: unknown): string {
  if (!(response instanceof HttpErrorResponse)) {
    return 'Something went wrong.';
  }

  if (response.status === 0) {
    return 'Cannot reach the API. Is the backend running?';
  }

  const body = response.error as ErrorInfo | string | null;

  if (typeof body === 'string') {
    return body;
  }

  if (body && typeof body === 'object') {
    const fieldErrors = body.fieldErrors ?? {};
    const fields = Object.entries(fieldErrors);

    if (fields.length > 0) {
      return fields.map(([field, message]) => `${field} ${message}`).join(', ');
    }

    if (body.message) {
      return body.message;
    }
  }

  return `Request failed with status ${response.status}.`;
}
