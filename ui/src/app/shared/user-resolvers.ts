import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import SessionService from '@core/session-service';
import { UsersService } from '@generated/openapi/services/users';
import { UserDto } from '@generated/openapi/models/user-dto';
import { firstValueFrom } from 'rxjs';

export const currentUserResolver: ResolveFn<UserDto | null> = () => {
  const sessionService = inject(SessionService);

  return sessionService.fetchMe$();
};

export const profileResolver: ResolveFn<UserDto | null> = async (route) => {
  const usersService = inject(UsersService);
  const userId = route.paramMap.get('owner');
  if (userId) {
    const user = await firstValueFrom(usersService.getUser(userId));
    return user;
  }
  return null;
};
