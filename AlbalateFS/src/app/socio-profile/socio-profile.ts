import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService, CurrentUser } from '../core/services/auth.service';
import { SocioService, Socio } from '../core/services/socio.service';

@Component({
  selector: 'app-socio-profile',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './socio-profile.html',
  styleUrl: './socio-profile.css'
})
export class SocioProfileComponent implements OnInit {
  currentUser: CurrentUser | null = null;
  socio = signal<Socio | null>(null);
  isLoading = signal(true);
  errorMessage = signal('');

  constructor(
    private authService: AuthService,
    private socioService: SocioService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    // En MVP, solo socios pueden ver su perfil
    if (this.currentUser.rol !== 'SOCIO') {
      this.router.navigate(['/']);
      return;
    }

    // Cargar datos del socio (asumiendo que existe endpoint GET /socios/{id})
    // Por ahora, solo mostramos datos del usuario autenticado
    this.isLoading.set(false);
  }

  irAHacerseSocio(): void {
    this.router.navigate(['/socio']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
