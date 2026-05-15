import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SocioRequest {
  nombre: string;
  apellidos: string;
  dni: string;
  email: string;
  telefono: string;
}

export interface SocioResponse {
  id: number;
  nombre: string;
  apellidos: string;
  dni: string;
  email: string;
  telefono: string;
  fechaAlta: string;
}

@Injectable({ providedIn: 'root' })
export class SocioService {
  private url = `${environment.apiUrl}/socios`;

  constructor(private http: HttpClient) {}

  create(socio: SocioRequest): Observable<SocioResponse> {
    return this.http.post<SocioResponse>(this.url, socio);
  }
}
