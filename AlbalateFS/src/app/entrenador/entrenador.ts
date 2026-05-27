import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { JugadorService, Jugador } from '../core/services/jugador.service';
import { EstadisticaService, EstadisticaJugador, EstadisticaRequest } from '../core/services/estadistica.service';
import { VideoService, VideoTactico, VideoRequest } from '../core/services/video.service';
import { PartidoService, Partido } from '../core/services/partido.service';
import { AlertaService, AlertaInfo } from '../core/services/alerta.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-entrenador',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './entrenador.html',
  styleUrls: ['./entrenador.css']
})
export class EntrenadorComponent implements OnInit {
  jugadores: Jugador[] = [];
  partidos: Partido[] = [];
  todosVideos: VideoTactico[] = [];

  selectedJugador: Jugador | null = null;
  activeTab: 'stats' | 'videos' = 'stats';

  // Estadísticas del jugador seleccionado
  estadisticas: EstadisticaJugador[] = [];
  editingStatId: number | null = null;
  editingStat: EstadisticaRequest = { goles: 0, asistencias: 0, minutos: 0, calificacion: 0 };

  // Formulario nueva estadística
  showNewStatForm = false;
  newStat: EstadisticaRequest = { jugadorId: 0, partidoId: 0, goles: 0, asistencias: 0, minutos: 0, calificacion: 0 };

  // Videos del jugador seleccionado
  videosJugador: VideoTactico[] = [];
  editingVideoId: number | null = null;
  editingVideo: VideoRequest = { url: '', descripcion: '', fecha: '', jugadores: [] };

  // Formulario nuevo vídeo
  showNewVideoForm = false;
  newVideo: VideoRequest = { url: '', descripcion: '', fecha: new Date().toISOString().slice(0, 16), jugadores: [] };

  isLoading = true;
  savingError = '';

  // Vista principal: jugador o alertas
  mainView: 'jugador' | 'alertas' = 'jugador';

  // Alertas
  alertasEnviadas: AlertaInfo[] = [];
  nuevaAlertaMensaje = '';
  nuevaAlertaJugadorIds: number[] = [];
  enviandoAlerta = false;
  alertaExito = '';
  alertaError = '';

  constructor(
    private authService: AuthService,
    private jugadorService: JugadorService,
    private estadisticaService: EstadisticaService,
    private videoService: VideoService,
    private partidoService: PartidoService,
    private alertaService: AlertaService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    forkJoin({
      jugadores: this.jugadorService.getAll(),
      partidos: this.partidoService.getAll(),
      videos: this.videoService.getAll()
    }).subscribe({
      next: ({ jugadores, partidos, videos }) => {
        // Excluir el propio entrenador de la lista de jugadores
        this.jugadores = jugadores.filter(j => j.posicion !== 'Entrenador');
        this.partidos = partidos.filter(p => p.estado === 'FINALIZADO');
        this.todosVideos = videos;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.isLoading = false; this.cdr.detectChanges(); }
    });
  }

  selectJugador(j: Jugador): void {
    this.selectedJugador = j;
    this.activeTab = 'stats';
    this.editingStatId = null;
    this.editingVideoId = null;
    this.showNewStatForm = false;
    this.showNewVideoForm = false;
    this.loadJugadorData(j.id);
  }

  loadJugadorData(jugadorId: number): void {
    forkJoin({
      stats: this.estadisticaService.getByJugador(jugadorId),
      videos: this.videoService.getAll()
    }).subscribe({
      next: ({ stats, videos }) => {
        this.estadisticas = stats.sort((a, b) =>
          new Date(b.partido.fechaHora).getTime() - new Date(a.partido.fechaHora).getTime()
        );
        this.todosVideos = videos;
        this.videosJugador = videos.filter(v => v.jugadores.some(j => j.id === jugadorId));
        this.cdr.detectChanges();
      }
    });
  }

  // ── ESTADÍSTICAS ────────────────────────────────────────────────────────

  startEditStat(stat: EstadisticaJugador): void {
    this.editingStatId = stat.id;
    this.editingStat = { goles: stat.goles, asistencias: stat.asistencias, minutos: stat.minutos, calificacion: stat.calificacion };
    this.cdr.detectChanges();
  }

  cancelEditStat(): void {
    this.editingStatId = null;
    this.cdr.detectChanges();
  }

  saveStat(stat: EstadisticaJugador): void {
    this.estadisticaService.update(stat.id, this.editingStat).subscribe({
      next: (updated) => {
        const idx = this.estadisticas.findIndex(s => s.id === stat.id);
        if (idx !== -1) this.estadisticas[idx] = { ...this.estadisticas[idx], ...updated };
        this.editingStatId = null;
        this.cdr.detectChanges();
      },
      error: () => { this.savingError = 'Error al guardar estadística.'; this.cdr.detectChanges(); }
    });
  }

  deleteStat(stat: EstadisticaJugador): void {
    if (!confirm(`¿Eliminar estadística del partido ${this.getPartidoLabel(stat.partido)}?`)) return;
    this.estadisticaService.delete(stat.id).subscribe({
      next: () => {
        this.estadisticas = this.estadisticas.filter(s => s.id !== stat.id);
        this.cdr.detectChanges();
      }
    });
  }

  openNewStatForm(): void {
    this.showNewStatForm = true;
    this.newStat = { jugadorId: this.selectedJugador!.id, partidoId: 0, goles: 0, asistencias: 0, minutos: 0, calificacion: 0 };
    this.cdr.detectChanges();
  }

  submitNewStat(): void {
    if (!this.newStat.partidoId) return;
    this.estadisticaService.create(this.newStat).subscribe({
      next: (created) => {
        this.estadisticas.unshift(created);
        this.showNewStatForm = false;
        this.cdr.detectChanges();
      },
      error: () => { this.savingError = 'Error al crear estadística.'; this.cdr.detectChanges(); }
    });
  }

  // ── VIDEOS ──────────────────────────────────────────────────────────────

  isVideoAsignadoAlJugador(video: VideoTactico): boolean {
    return video.jugadores.some(j => j.id === this.selectedJugador?.id);
  }

  toggleAsignacion(video: VideoTactico): void {
    const yaAsignado = this.isVideoAsignadoAlJugador(video);
    const nuevosJugadores = yaAsignado
      ? video.jugadores.filter(j => j.id !== this.selectedJugador!.id)
      : [...video.jugadores, { id: this.selectedJugador!.id }];
    const req: VideoRequest = {
      url: video.url,
      descripcion: video.descripcion,
      fecha: video.fecha,
      jugadores: nuevosJugadores.map(j => ({ id: j.id }))
    };
    this.videoService.update(video.id, req).subscribe({
      next: (updated) => {
        this.todosVideos = this.todosVideos.map(v => v.id === updated.id ? updated : v);
        this.videosJugador = this.todosVideos.filter(v => v.jugadores.some(j => j.id === this.selectedJugador?.id));
        this.cdr.detectChanges();
      }
    });
  }

  asignarATodos(video: VideoTactico): void {
    if (!confirm(`¿Asignar "${video.descripcion}" a todos los jugadores?`)) return;
    this.videoService.asignarATodos(video.id).subscribe({
      next: (updated) => {
        this.todosVideos = this.todosVideos.map(v => v.id === updated.id ? updated : v);
        this.videosJugador = this.todosVideos.filter(v => v.jugadores.some(j => j.id === this.selectedJugador?.id));
        this.cdr.detectChanges();
      }
    });
  }

  isJugadorEnEdicion(id: number): boolean {
    return this.editingVideo.jugadores.some(j => j.id === id);
  }

  toggleJugadorEnEdicion(id: number, checked: boolean): void {
    if (checked) {
      if (!this.editingVideo.jugadores.some(j => j.id === id)) {
        this.editingVideo.jugadores = [...this.editingVideo.jugadores, { id }];
      }
    } else {
      this.editingVideo.jugadores = this.editingVideo.jugadores.filter(j => j.id !== id);
    }
  }

  startEditVideo(video: VideoTactico): void {
    this.editingVideoId = video.id;
    this.editingVideo = {
      url: video.url,
      descripcion: video.descripcion,
      fecha: video.fecha ? video.fecha.slice(0, 16) : '',
      jugadores: video.jugadores.map(j => ({ id: j.id }))
    };
    this.cdr.detectChanges();
  }

  cancelEditVideo(): void {
    this.editingVideoId = null;
    this.cdr.detectChanges();
  }

  saveVideo(video: VideoTactico): void {
    const req: VideoRequest = {
      ...this.editingVideo,
      fecha: this.editingVideo.fecha + ':00'
    };
    this.videoService.update(video.id, req).subscribe({
      next: (updated) => {
        this.todosVideos = this.todosVideos.map(v => v.id === updated.id ? updated : v);
        this.videosJugador = this.todosVideos.filter(v => v.jugadores.some(j => j.id === this.selectedJugador?.id));
        this.editingVideoId = null;
        this.cdr.detectChanges();
      }
    });
  }

  deleteVideo(video: VideoTactico): void {
    if (!confirm(`¿Eliminar el vídeo "${video.descripcion}"?`)) return;
    this.videoService.delete(video.id).subscribe({
      next: () => {
        this.todosVideos = this.todosVideos.filter(v => v.id !== video.id);
        this.videosJugador = this.todosVideos.filter(v => v.jugadores.some(j => j.id === this.selectedJugador?.id));
        this.cdr.detectChanges();
      }
    });
  }

  openNewVideoForm(): void {
    this.showNewVideoForm = true;
    this.newVideo = {
      url: '',
      descripcion: '',
      fecha: new Date().toISOString().slice(0, 16),
      jugadores: this.selectedJugador ? [{ id: this.selectedJugador.id }] : []
    };
    this.cdr.detectChanges();
  }

  submitNewVideo(): void {
    if (!this.newVideo.url || !this.newVideo.descripcion) return;
    const req: VideoRequest = { ...this.newVideo, fecha: this.newVideo.fecha + ':00' };
    this.videoService.create(req).subscribe({
      next: (created) => {
        this.todosVideos.push(created);
        this.videosJugador = this.todosVideos.filter(v => v.jugadores.some(j => j.id === this.selectedJugador?.id));
        this.showNewVideoForm = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ── HELPERS ─────────────────────────────────────────────────────────────

  getPartidoLabel(partido: any): string {
    if (!partido) return '-';
    const d = new Date(partido.fechaHora);
    const fecha = d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' });
    return `${partido.local} vs ${partido.visitante} (${fecha})`;
  }

  getPartidosDisponibles(): Partido[] {
    const usados = new Set(this.estadisticas.map(e => e.partido?.id));
    return this.partidos.filter(p => !usados.has(p.id));
  }

  getCalificacionMedia(): string {
    if (!this.estadisticas.length) return '—';
    const sum = this.estadisticas.reduce((acc, s) => acc + s.calificacion, 0);
    return (sum / this.estadisticas.length).toFixed(1);
  }

  cerrarSesion(): void {
    this.router.navigate(['/']);
  }

  // ── ALERTAS ─────────────────────────────────────────────────────────────

  mostrarAlertas(): void {
    this.mainView = 'alertas';
    this.selectedJugador = null;
    this.alertaService.getAll().subscribe({
      next: (alertas) => {
        this.alertasEnviadas = alertas.sort((a, b) =>
          new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
        );
        this.cdr.detectChanges();
      }
    });
  }

  toggleJugadorAlerta(id: number, checked: boolean): void {
    if (checked) {
      if (!this.nuevaAlertaJugadorIds.includes(id)) {
        this.nuevaAlertaJugadorIds = [...this.nuevaAlertaJugadorIds, id];
      }
    } else {
      this.nuevaAlertaJugadorIds = this.nuevaAlertaJugadorIds.filter(j => j !== id);
    }
  }

  enviarAlerta(): void {
    if (!this.nuevaAlertaMensaje.trim()) return;
    this.enviandoAlerta = true;
    this.alertaExito = '';
    this.alertaError = '';
    const req = {
      mensaje: this.nuevaAlertaMensaje.trim(),
      jugadorIds: this.nuevaAlertaJugadorIds.length > 0 ? this.nuevaAlertaJugadorIds : undefined
    };
    this.alertaService.crear(req).subscribe({
      next: (creada) => {
        this.alertasEnviadas.unshift(creada);
        this.nuevaAlertaMensaje = '';
        this.nuevaAlertaJugadorIds = [];
        this.enviandoAlerta = false;
        this.alertaExito = 'Alerta enviada correctamente.';
        this.cdr.detectChanges();
        setTimeout(() => { this.alertaExito = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: () => {
        this.alertaError = 'Error al enviar la alerta. Inténtalo de nuevo.';
        this.enviandoAlerta = false;
        this.cdr.detectChanges();
      }
    });
  }

  eliminarAlerta(alerta: AlertaInfo): void {
    if (!confirm(`¿Eliminar esta alerta?`)) return;
    this.alertaService.delete(alerta.id).subscribe({
      next: () => {
        this.alertasEnviadas = this.alertasEnviadas.filter(a => a.id !== alerta.id);
        this.cdr.detectChanges();
      }
    });
  }
}
