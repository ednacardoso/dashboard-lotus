import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const roleGuard = (...allowedRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isAuthenticated() && allowedRoles.includes(authService.getRole() || '')) {
      return true;
    }

    return router.parseUrl('/login');
  };
};
