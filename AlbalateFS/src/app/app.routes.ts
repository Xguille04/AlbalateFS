import { Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { LoginComponent } from './login/login';
import { DashboardComponent } from './dashboard/dashboard';
import { EntrenadorComponent } from './entrenador/entrenador';
import { SocioFormComponent } from './socio-form/socio-form';
import { PlantillaComponent } from './plantilla/plantilla';
import { CalendarioComponent } from './calendario/calendario';
import { EstadisticasComponent } from './estadisticas/estadisticas';
import { TiendaComponent } from './tienda/tienda';
import { authGuard } from './core/guards/auth.guard';
import { entrenadorGuard } from './core/guards/entrenador.guard';

export const routes: Routes = [
  // Ruta raiz: Carga el portal publico por defecto para los aficionados
  { path: '', component: HomeComponent },

  // Rutas del area privada y autenticacion
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'entrenador', component: EntrenadorComponent, canActivate: [entrenadorGuard] },

  // Ruta publica para la plantilla de jugadores
  { path: 'plantilla', component: PlantillaComponent },

  // Ruta para el formulario de alta de socios
  { path: 'socio', component: SocioFormComponent },

  { path: 'calendario', component: CalendarioComponent },

  { path: 'estadisticas', component: EstadisticasComponent },

  // Tienda online
  { path: 'tienda', component: TiendaComponent },

  // Ruta comodin (Wildcard): SIEMPRE AL FINAL
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

