import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Jugador {
  id: number;
  nombre: string;
  apellidos: string;
  dorsal: number;
  posicion: string;
  fotoUrl: string;
  fechaNacimiento: string;
  temporadasEnElClub: number;
  piernasDominante: string;
}

@Injectable({ providedIn: 'root' })
export class JugadorService {
  private url = `${environment.apiUrl}/jugadores`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Jugador[]> {
    return this.http.get<Jugador[]>(this.url);
  }

  getByUsuarioId(usuarioId: number): Observable<Jugador> {
    return this.http.get<Jugador>(`${this.url}/usuario/${usuarioId}`);
  }
}
