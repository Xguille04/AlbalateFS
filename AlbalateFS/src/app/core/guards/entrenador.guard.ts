import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const entrenadorGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const user = authService.getCurrentUser();

  if (user && user.rol === 'ENTRENADOR' && !authService.isTokenExpired()) {
    return true;
  }

  authService.logout();
  return router.createUrlTree(['/login']);
};
