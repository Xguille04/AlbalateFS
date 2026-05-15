import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { PartidoService, Partido } from '../core/services/partido.service';
import { EstadisticaService, EstadisticaJugador } from '../core/services/estadistica.service';

interface EquipoStats {
  partidosJugados: number;
  victorias: number;
  empates: number;
  derrotas: number;
  golesFavor: number;
  golesContra: number;
}

interface RankingItem {
  posicion: number;
  nombre: string;
  dorsal: number;
  goles?: number;
  asistencias?: number;
}

@Component({
  selector: 'app-estadisticas',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './estadisticas.html'
})
export class EstadisticasComponent implements OnInit {
  isLoading = true;
  errorMessage = '';

  equipo: EquipoStats = {
    partidosJugados: 0, victorias: 0, empates: 0, derrotas: 0,
    golesFavor: 0, golesContra: 0
  };

  goleadores: RankingItem[] = [];
  asistentes: RankingItem[] = [];

  private readonly NOMBRE_EQUIPO = 'Albalate FS';

  constructor(
    private partidoService: PartidoService,
    private estadisticaService: EstadisticaService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    forkJoin({
      partidos: this.partidoService.getAll(),
      estadisticas: this.estadisticaService.getAll()
    }).subscribe({
      next: ({ partidos, estadisticas }) => {
        this.computeEquipoStats(partidos);
        this.computeRankings(estadisticas);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las estadísticas. Inténtalo más tarde.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private computeEquipoStats(partidos: Partido[]): void {
    const finalizados = partidos.filter(p => p.estado === 'FINALIZADO');
    this.equipo.partidosJugados = finalizados.length;
    for (const p of finalizados) {
      const esLocal = p.local.includes(this.NOMBRE_EQUIPO);
      const golesMios = esLocal ? (p.golesLocal ?? 0) : (p.golesVisitante ?? 0);
      const golesRival = esLocal ? (p.golesVisitante ?? 0) : (p.golesLocal ?? 0);
      this.equipo.golesFavor += golesMios;
      this.equipo.golesContra += golesRival;
      if (golesMios > golesRival) this.equipo.victorias++;
      else if (golesMios === golesRival) this.equipo.empates++;
      else this.equipo.derrotas++;
    }
  }

  private computeRankings(estadisticas: EstadisticaJugador[]): void {
    const totales = new Map<number, { nombre: string; apellidos: string; dorsal: number; goles: number; asistencias: number }>();
    for (const e of estadisticas) {
      const id = e.jugador.id;
      if (!totales.has(id)) {
        totales.set(id, { nombre: e.jugador.nombre, apellidos: e.jugador.apellidos, dorsal: e.jugador.dorsal, goles: 0, asistencias: 0 });
      }
      const entry = totales.get(id)!;
      entry.goles += e.goles;
      entry.asistencias += e.asistencias;
    }

    const arr = Array.from(totales.values());

    this.goleadores = arr
      .filter(j => j.goles > 0)
      .sort((a, b) => b.goles - a.goles)
      .slice(0, 5)
      .map((j, i) => ({ posicion: i + 1, nombre: `${j.nombre} ${j.apellidos}`, dorsal: j.dorsal, goles: j.goles }));

    this.asistentes = arr
      .filter(j => j.asistencias > 0)
      .sort((a, b) => b.asistencias - a.asistencias)
      .slice(0, 5)
      .map((j, i) => ({ posicion: i + 1, nombre: `${j.nombre} ${j.apellidos}`, dorsal: j.dorsal, asistencias: j.asistencias }));
  }
}