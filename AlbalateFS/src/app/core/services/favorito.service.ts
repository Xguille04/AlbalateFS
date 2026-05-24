import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Producto } from './producto.service';

export interface Favorito {
  id: number;
  producto: Producto;
}

@Injectable({ providedIn: 'root' })
export class FavoritoService {
  private readonly apiUrl = `${environment.apiUrl}/favoritos`;

  constructor(private http: HttpClient) {}

  getByUsuario(usuarioId: number): Observable<Favorito[]> {
    return this.http.get<Favorito[]>(`${this.apiUrl}/usuario/${usuarioId}`);
  }

  toggle(usuarioId: number, productoId: number): Observable<any> {
    return this.http.post<any>(this.apiUrl, { usuarioId, productoId });
  }
}
