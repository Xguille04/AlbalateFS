import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AlertaRemitente {
  id: number;
  email: string;
  rol: string;
}

export interface AlertaInfo {
  id: number;
  mensaje: string;
  fecha: string;
  remitente: AlertaRemitente;
}

export interface AlertaJugador {
  id: number;
  alerta: AlertaInfo;
  leida: boolean;
}

export interface AlertaRequest {
  mensaje: string;
  jugadorIds?: number[];
}

@Injectable({ providedIn: 'root' })
export class AlertaService {
  private url = `${environment.apiUrl}/alertas`;

  constructor(private http: HttpClient) {}

  /** Jugador: obtiene sus alertas */
  getMisAlertas(jugadorId: number): Observable<AlertaJugador[]> {
    return this.http.get<AlertaJugador[]>(`${this.url}/mis-alertas/${jugadorId}`);
  }

  /** Jugador: marca una alerta como leída */
  marcarLeida(alertaId: number, jugadorId: number): Observable<AlertaJugador> {
    return this.http.patch<AlertaJugador>(`${this.url}/${alertaId}/leer/${jugadorId}`, {});
  }

  /** Entrenador: crea y envía una alerta */
  crear(req: AlertaRequest): Observable<AlertaInfo> {
    return this.http.post<AlertaInfo>(this.url, req);
  }

  /** Entrenador: obtiene todas las alertas */
  getAll(): Observable<AlertaInfo[]> {
    return this.http.get<AlertaInfo[]>(this.url);
  }

  /** Entrenador: elimina una alerta */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
