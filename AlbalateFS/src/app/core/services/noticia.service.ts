import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Noticia {
  id: number;
  titulo: string;
  resumen: string;
  contenido: string;
  fechaPublicacion: string;
  etiqueta: string;
  imagenUrl: string;
}

@Injectable({ providedIn: 'root' })
export class NoticiaService {
  private url = `${environment.apiUrl}/noticias`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Noticia[]> {
    return this.http.get<Noticia[]>(this.url);
  }
}
