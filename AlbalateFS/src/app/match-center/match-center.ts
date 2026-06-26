import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PartidoService, Partido } from '../core/services/partido.service';

@Component({
  selector: 'app-match-center',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './match-center.html',
  styleUrl: './match-center.css'
})
export class MatchCenterComponent implements OnInit {
  partidos = signal<Partido[]>([]);
  isLoading = signal(true);
  activeTab = signal<'proximos' | 'resultados'>('proximos');
  readonly NOMBRE_CLUB = 'Albalate FS';

  constructor(private partidoService: PartidoService) {}

  ngOnInit(): void {
    this.partidoService.getAll().subscribe({
      next: (partidos) => {
        this.partidos.set(partidos);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  getProximos(): Partido[] {
    return this.partidos()
      .filter(p => p.estado?.toLowerCase() !== 'finalizado')
      .sort((a, b) => new Date(a.fecha || 0).getTime() - new Date(b.fecha || 0).getTime());
  }

  getResultados(): Partido[] {
    return this.partidos()
      .filter(p => p.estado?.toLowerCase() === 'finalizado')
      .sort((a, b) => new Date(b.fecha || 0).getTime() - new Date(a.fecha || 0).getTime());
  }

  isLocal(partido: Partido): boolean {
    return partido.local === this.NOMBRE_CLUB;
  }

  getResultado(partido: Partido): string {
    const somoLocal = this.isLocal(partido);
    const nuestros = somoLocal ? (partido.golesLocal ?? 0) : (partido.golesVisitante ?? 0);
    const suyos = somoLocal ? (partido.golesVisitante ?? 0) : (partido.golesLocal ?? 0);
    return `${nuestros} - ${suyos}`;
  }

  getVictoria(partido: Partido): 'victoria' | 'empate' | 'derrota' | null {
    const somoLocal = this.isLocal(partido);
    const nuestros = somoLocal ? (partido.golesLocal ?? 0) : (partido.golesVisitante ?? 0);
    const suyos = somoLocal ? (partido.golesVisitante ?? 0) : (partido.golesLocal ?? 0);
    if (nuestros > suyos) return 'victoria';
    if (nuestros === suyos) return 'empate';
    return 'derrota';
  }

  formatFecha(fecha?: string | Date): string {
    if (!fecha) return '';
    const date = typeof fecha === 'string' ? new Date(fecha) : fecha;
    return date.toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
  }
}
