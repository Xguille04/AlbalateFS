import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { JugadorService, Jugador } from '../core/services/jugador.service';

@Component({
  selector: 'app-plantilla',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './plantilla.html'
})
export class PlantillaComponent implements OnInit {
  jugadores: Jugador[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(private jugadorService: JugadorService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.jugadorService.getAll().subscribe({
      next: (data) => {
        this.jugadores = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los jugadores. Comprueba que el servidor esta activo.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
