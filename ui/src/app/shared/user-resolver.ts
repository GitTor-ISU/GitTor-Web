import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import SessionService from '@core/session-service';
import { UserDto } from '@generated/openapi/models/user-dto';

export const userResolver: ResolveFn<UserDto | null> = () => {
  const sessionService = inject(SessionService);

  return sessionService.fetchMe$();
};
