import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification-service';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const notificationService = inject(NotificationService);
  const apiReq = req.clone({
    url: `http://localhost:8080${req.url}`,
    headers: req.headers.set('Content-Type', 'application/json')
  });

  return next(apiReq).pipe(
    catchError(error => {
      let message = 'An error occurred';
      
      if (error.status === 0) {
        message = 'Unable to connect to server. Please check if the backend is running.';
      } else if (error.error instanceof ErrorEvent) {
        message = `Client error: ${error.error.message}`;
      } else {
        message = `Server error: ${error.status} - ${error.message}`;
      }

      notificationService.show({
        type: 'error',
        message
      });

      return throwError(() => error);
    })
  );
};