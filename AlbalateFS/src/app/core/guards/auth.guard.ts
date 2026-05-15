import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.getToken()) {
    return router.createUrlTree(['/login']);
  }

  const user = authService.getCurrentUser();
  if (user?.rol === 'SOCIO') {
    // Los socios tienen cuenta pero no acceso al área privada de jugadores
    return router.createUrlTree(['/']);
  }

  return true;
};
