import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { NoticiaService, Noticia } from '../core/services/noticia.service';
import { PartidoService, Partido } from '../core/services/partido.service';
import { AuthService } from '../core/services/auth.service';

interface PartidoCarrusel {
  rival: string;
  resultado: string;
  victoria: boolean | null;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent implements OnInit {
  currentYear = new Date().getFullYear();
  isLoading = true;
  menuAbierto = false;

  noticias: Noticia[] = [];
  private allNoticias: Noticia[] = [];
  ultimoPartido: Partido | null = null;
  proximoPartido: Partido | null = null;
  ultimosTresPartidos: PartidoCarrusel[] = [];

  private readonly NOMBRE_CLUB = 'Albalate FS';

  constructor(
    private noticiaService: NoticiaService,
    private partidoService: PartidoService,
    private cdr: ChangeDetectorRef,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    forkJoin({
      noticias: this.noticiaService.getAll(),
      partidos: this.partidoService.getAll()
    }).subscribe({
      next: ({ noticias, partidos }) => {
        this.allNoticias = noticias;
        this.noticias = this.getRandomNoticias();

        const finalizados = partidos.filter(p => p.estado?.toLowerCase() === 'finalizado');
        this.ultimoPartido = finalizados.at(-1) ?? null;
        this.proximoPartido = partidos.find(p => p.estado?.toLowerCase() !== 'finalizado') ?? null;

        this.ultimosTresPartidos = finalizados.slice(-3).reverse().map(p => {
          const somoLocal = p.local === this.NOMBRE_CLUB;
          const rival = somoLocal ? p.visitante : p.local;
          const nuestros = somoLocal ? (p.golesLocal ?? 0) : (p.golesVisitante ?? 0);
          const suyos = somoLocal ? (p.golesVisitante ?? 0) : (p.golesLocal ?? 0);
          const victoria = nuestros > suyos ? true : nuestros === suyos ? null : false;
          return { rival, resultado: `${nuestros} - ${suyos}`, victoria };
        });

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  recargarNoticias(): void {
    this.noticias = this.getRandomNoticias();
    this.cdr.detectChanges();
  }

  private getRandomNoticias(): Noticia[] {
    const shuffled = [...this.allNoticias].sort(() => Math.random() - 0.5);
    return shuffled.slice(0, 3);
  }

  getResultadoLabel(p: Partido): string {
    return `${p.golesLocal ?? 0} - ${p.golesVisitante ?? 0}`;
  }

  formatFecha(fechaHora: string): string {
    return new Date(fechaHora).toLocaleDateString('es-ES', {
      weekday: 'long', day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit'
    });
  }
}
