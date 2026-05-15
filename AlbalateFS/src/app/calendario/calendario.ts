import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PartidoService, Partido } from '../core/services/partido.service';

interface PartidoVista {
  jornada: number;
  fecha: string;
  hora: string;
  local: string;
  visitante: string;
  resultado: string;
  lugar: string;
  estado: string;
  golesLocal: number | null;
  golesVisitante: number | null;
}

@Component({
  selector: 'app-calendario',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './calendario.html'
})
export class CalendarioComponent implements OnInit {
  partidos: PartidoVista[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(private partidoService: PartidoService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.partidoService.getAll().subscribe({
      next: (data) => {
        this.partidos = data.map((p, index) => this.mapPartido(p, index + 1));
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudo cargar el calendario. Comprueba que el servidor esta activo.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private mapPartido(p: Partido, jornada: number): PartidoVista {
    const dt = new Date(p.fechaHora);
    const fecha = dt.toLocaleDateString('es-ES', { day: 'numeric', month: 'long', year: 'numeric' });
    const hora = dt.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }) + 'h';
    const finalizado = p.estado?.toLowerCase() === 'finalizado';
    const resultado = finalizado
      ? `${p.golesLocal ?? 0} - ${p.golesVisitante ?? 0}`
      : '-';

    return { jornada, fecha, hora, local: p.local, visitante: p.visitante, resultado, lugar: p.lugar, estado: p.estado, golesLocal: p.golesLocal ?? null, golesVisitante: p.golesVisitante ?? null };
  }

  getBorderClass(partido: PartidoVista): string {
    if (partido.estado?.toLowerCase() !== 'finalizado') return 'border-l-amber-400';
    if (partido.golesLocal === null || partido.golesVisitante === null) return 'border-l-gray-400';
    if (partido.golesLocal > partido.golesVisitante) return 'border-l-green-500';
    if (partido.golesLocal < partido.golesVisitante) return 'border-l-red-500';
    return 'border-l-gray-400';
  }
}
