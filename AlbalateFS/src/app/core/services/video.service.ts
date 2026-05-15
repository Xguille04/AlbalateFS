import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface VideoTactico {
  id: number;
  url: string;
  descripcion: string;
  fecha: string;
  jugadores: { id: number; nombre: string; apellidos: string }[];
}

export interface VideoRequest {
  url: string;
  descripcion: string;
  fecha: string;
  jugadores: { id: number }[];
}

@Injectable({ providedIn: 'root' })
export class VideoService {
  private url = `${environment.apiUrl}/videos`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<VideoTactico[]> {
    return this.http.get<VideoTactico[]>(this.url);
  }

  create(req: VideoRequest): Observable<VideoTactico> {
    return this.http.post<VideoTactico>(this.url, req);
  }

  update(id: number, req: VideoRequest): Observable<VideoTactico> {
    return this.http.put<VideoTactico>(`${this.url}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  asignarATodos(id: number): Observable<VideoTactico> {
    return this.http.post<VideoTactico>(`${this.url}/${id}/asignar-todos`, {});
  }
}
