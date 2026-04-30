import { inject } from '@angular/core';
import { ResolveFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import SessionService from '@core/session-service';
import { TorrentDto } from '@generated/openapi/models/torrent-dto';
import { UserDto } from '@generated/openapi/models/user-dto';
import { UsersService } from '@generated/openapi/services/users';

export const currentUserResolver: ResolveFn<UserDto | null> = () => {
  const sessionService = inject(SessionService);

  return sessionService.fetchMe$();
};

export const profileResolver: ResolveFn<UserDto | null> = async (route) => {
  const usersService = inject(UsersService);
  const sessionService = inject(SessionService);
  const router = inject(Router);
  const userId = route.paramMap.get('owner');
  if (userId) {
    const user = await firstValueFrom(usersService.getUser(userId))
      .then((user) => user ?? null)
      .catch(() => null);

    if (!user) {
      router.navigate(['404']);
      return null;
    }

    return {
      ...user,
      isCurrentUser: sessionService.hasToken() && sessionService.user()?.id === user.id,
    };
  }
  return null;
};

export const profileTorrentsResolver: ResolveFn<TorrentDto[]> = async (route) => {
  const usersService = inject(UsersService);
  const userId = route.paramMap.get('owner');
  if (userId) {
    const torrents = await firstValueFrom(usersService.getUserTorrents(userId));
    return torrents;
  }
  return [];
};
