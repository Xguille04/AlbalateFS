import { Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { authGuard } from './core/guards/auth.guard';
import { entrenadorGuard } from './core/guards/entrenador.guard';

export const routes: Routes = [
  // Ruta raiz: Carga el portal publico por defecto para los aficionados
  { path: '', component: HomeComponent },

  // Rutas del area privada y autenticacion (lazy loaded)
  {
    path: 'login',
    loadComponent: () => import('./login/login').then(m => m.LoginComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'entrenador',
    loadComponent: () => import('./entrenador/entrenador').then(m => m.EntrenadorComponent),
    canActivate: [entrenadorGuard]
  },

  // Ruta publica para la plantilla de jugadores (lazy loaded)
  {
    path: 'plantilla',
    loadComponent: () => import('./plantilla/plantilla').then(m => m.PlantillaComponent)
  },

  // Ruta para el formulario de alta de socios (lazy loaded)
  {
    path: 'socio',
    loadComponent: () => import('./socio-form/socio-form').then(m => m.SocioFormComponent)
  },

  // Calendario (lazy loaded)
  {
    path: 'calendario',
    loadComponent: () => import('./calendario/calendario').then(m => m.CalendarioComponent)
  },

  // Estadísticas (lazy loaded)
  {
    path: 'estadisticas',
    loadComponent: () => import('./estadisticas/estadisticas').then(m => m.EstadisticasComponent)
  },

  // Tienda online (lazy loaded)
  {
    path: 'tienda',
    loadComponent: () => import('./tienda/tienda').then(m => m.TiendaComponent)
  },

  // Ruta comodin (Wildcard): SIEMPRE AL FINAL
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

