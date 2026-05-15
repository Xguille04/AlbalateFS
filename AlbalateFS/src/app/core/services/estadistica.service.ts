import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Partido {
  id: number;
  fechaHora: string;
  local: string;
  visitante: string;
  golesLocal: number | null;
  golesVisitante: number | null;
  lugar: string;
  estado: string;
}

export interface EstadisticaJugador {
  id: number;
  jugador: { id: number; nombre: string; apellidos: string; dorsal: number };
  partido: Partido;
  goles: number;
  asistencias: number;
  minutos: number;
  calificacion: number;
}

export interface EstadisticaRequest {
  jugadorId?: number;
  partidoId?: number;
  goles: number;
  asistencias: number;
  minutos: number;
  calificacion: number;
}

@Injectable({ providedIn: 'root' })
export class EstadisticaService {
  private url = `${environment.apiUrl}/estadisticas`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<EstadisticaJugador[]> {
    return this.http.get<EstadisticaJugador[]>(this.url);
  }

  getByJugador(jugadorId: number): Observable<EstadisticaJugador[]> {
    return this.http.get<EstadisticaJugador[]>(`${this.url}/jugador/${jugadorId}`);
  }

  create(req: EstadisticaRequest): Observable<EstadisticaJugador> {
    return this.http.post<EstadisticaJugador>(this.url, req);
  }

  update(id: number, req: EstadisticaRequest): Observable<EstadisticaJugador> {
    return this.http.put<EstadisticaJugador>(`${this.url}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
