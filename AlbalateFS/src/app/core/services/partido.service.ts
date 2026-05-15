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

@Injectable({ providedIn: 'root' })
export class PartidoService {
  private url = `${environment.apiUrl}/partidos`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Partido[]> {
    return this.http.get<Partido[]>(this.url);
  }
}
