import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService, CurrentUser } from '../core/services/auth.service';
import { JugadorService, Jugador } from '../core/services/jugador.service';
import { EstadisticaService } from '../core/services/estadistica.service';
import { VideoService, VideoTactico } from '../core/services/video.service';
import { AlertaService, AlertaJugador } from '../core/services/alerta.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  currentUser: CurrentUser | null = null;
  isLoading = true;

  jugador = {
    nombre: '',
    apellidos: '',
    posicion: '-',
    dorsal: '-' as number | string,
    fotoUrl: '',
    fechaNacimiento: '',
    temporadasEnElClub: 0,
    piernasDominante: '',
    temporada: '25/26',
    rendimiento: '+12%',
    proximoEntreno: 'Miércoles 20:00'
  };

  estadisticas = {
    goles: 0,
    asistencias: 0,
    minutos: 0,
    calificacion: 0
  };

  correccionesVideo: VideoTactico[] = [];
  alertas: AlertaJugador[] = [];
  jugadorId = 0;

  constructor(
    private authService: AuthService,
    private jugadorService: JugadorService,
    private estadisticaService: EstadisticaService,
    private videoService: VideoService,
    private alertaService: AlertaService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    if (!this.currentUser) {
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    this.jugadorService.getByUsuarioId(this.currentUser.id).subscribe({
      next: (jugador) => {
        this.jugador.nombre = jugador.nombre;
        this.jugador.apellidos = jugador.apellidos;
        this.jugador.posicion = jugador.posicion;
        this.jugador.dorsal = jugador.dorsal;
        this.jugador.fotoUrl = jugador.fotoUrl;
        this.jugador.fechaNacimiento = jugador.fechaNacimiento;
        this.jugador.temporadasEnElClub = jugador.temporadasEnElClub;
        this.jugador.piernasDominante = jugador.piernasDominante;
        this.cdr.detectChanges();

        this.jugadorId = jugador.id;

        forkJoin({
          stats: this.estadisticaService.getByJugador(jugador.id),
          videos: this.videoService.getAll(),
          alertas: this.alertaService.getMisAlertas(jugador.id).pipe(catchError(() => of([])))
        }).subscribe({
          next: ({ stats, videos, alertas }) => {
            this.estadisticas.goles = stats.reduce((s, e) => s + e.goles, 0);
            this.estadisticas.asistencias = stats.reduce((s, e) => s + e.asistencias, 0);
            this.estadisticas.minutos = stats.reduce((s, e) => s + e.minutos, 0);
            const conCalif = stats.filter(e => e.calificacion > 0);
            this.estadisticas.calificacion = conCalif.length
              ? parseFloat((conCalif.reduce((s, e) => s + e.calificacion, 0) / conCalif.length).toFixed(1))
              : 0;
            this.correccionesVideo = videos.filter(v => v.jugadores?.some(j => j.id === jugador.id));
            this.alertas = alertas.sort((a, b) =>
              new Date(b.alerta.fecha).getTime() - new Date(a.alerta.fecha).getTime()
            );
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: () => { this.isLoading = false; this.cdr.detectChanges(); }
        });
      },
      error: () => { this.isLoading = false; this.cdr.detectChanges(); }
    });
  }

  cerrarSesion(): void {
    this.router.navigate(['/']);
  }

  getEdad(): number {
    if (!this.jugador.fechaNacimiento) return 0;
    const hoy = new Date();
    const nac = new Date(this.jugador.fechaNacimiento);
    let edad = hoy.getFullYear() - nac.getFullYear();
    const m = hoy.getMonth() - nac.getMonth();
    if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) edad--;
    return edad;
  }

  formatFechaNac(): string {
    if (!this.jugador.fechaNacimiento) return '-';
    return new Date(this.jugador.fechaNacimiento).toLocaleDateString('es-ES', {
      day: 'numeric', month: 'long', year: 'numeric'
    });
  }

  get alertasNoLeidas(): number {
    return this.alertas.filter(a => !a.leida).length;
  }

  marcarLeida(alerta: AlertaJugador): void {
    if (alerta.leida) return;
    this.alertaService.marcarLeida(alerta.alerta.id, this.jugadorId).subscribe({
      next: (updated) => {
        alerta.leida = updated.leida;
        this.cdr.detectChanges();
      }
    });
  }
}
