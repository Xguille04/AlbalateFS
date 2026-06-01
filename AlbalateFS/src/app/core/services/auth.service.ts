import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  id: number;
  email: string;
  rol: string;
}

export interface CurrentUser {
  id: number;
  email: string;
  rol: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';

  isLoggedIn = signal<boolean>(this.hasToken());

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<JwtResponse> {
    return this.http
      .post<JwtResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(
        tap((response) => {
          localStorage.setItem(this.TOKEN_KEY, response.token);
          localStorage.setItem(
            this.USER_KEY,
            JSON.stringify({ id: response.id, email: response.email, rol: response.rol })
          );
          this.isLoggedIn.set(true);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.isLoggedIn.set(false);
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  getCurrentUser(): CurrentUser | null {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) : null;
  }

  private hasToken(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (payload.exp * 1000 < Date.now()) {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        return false;
      }
      return true;
    } catch {
      return false;
    }
  }
}
