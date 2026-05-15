import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProductoService, Producto } from '../core/services/producto.service';
import { FavoritoService } from '../core/services/favorito.service';
import { AuthService } from '../core/services/auth.service';
import { StripeService } from '../core/services/stripe.service';

@Component({
  selector: 'app-tienda',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './tienda.html',
  styleUrl: './tienda.css'
})
export class TiendaComponent implements OnInit {
  allProductos: Producto[] = [];
  productosFiltrados: Producto[] = [];
  favoritosIds = new Set<number>();
  isLoading = true;

  // Filtros
  searchQuery = '';
  categoriaActiva = 'Todos';
  mostrarSoloFavoritos = false;
  ordenActivo = 'nombre';

  readonly categorias = ['Todos', 'Ropa', 'Accesorios', 'Equipamiento'];
  readonly ordenes = [
    { value: 'nombre',     label: 'Nombre A-Z' },
    { value: 'precio_asc', label: 'Precio ↑' },
    { value: 'precio_desc',label: 'Precio ↓' },
    { value: 'destacado',  label: 'Destacados' }
  ];

  // Carrito (local)
  carrito: { producto: Producto; cantidad: number }[] = [];
  carritoAbierto = false;

  // ── Checkout Stripe ──────────────────────────────────────────────────────
  checkoutAbierto = false;
  /** 'contacto' | 'pago' | 'confirmacion' */
  checkoutStep: 'contacto' | 'pago' | 'confirmacion' = 'contacto';
  checkoutEmail = '';
  checkoutNombre = '';
  checkoutEmailError = '';
  checkoutError = '';
  checkoutIsLoading = false;

  private checkoutStripe: any = null;
  private checkoutElements: any = null;
  private checkoutPaymentElement: any = null;
  checkoutStripeReady = false;
  checkoutStripeError = '';

  constructor(
    private productoService: ProductoService,
    private favoritoService: FavoritoService,
    public authService: AuthService,
    private stripeService: StripeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.productoService.getAll().subscribe({
      next: (productos) => {
        this.allProductos = productos;
        this.aplicarFiltros();
        this.isLoading = false;
        const user = this.authService.getCurrentUser();
        if (user) {
          this.favoritoService.getByUsuario(user.id).subscribe({
            next: (favs) => {
              this.favoritosIds = new Set(favs.map(f => f.producto.id));
              this.cdr.detectChanges();
            }
          });
        }
        this.cdr.detectChanges();
      },
      error: () => { this.isLoading = false; this.cdr.detectChanges(); }
    });
  }

  aplicarFiltros(): void {
    let result = [...this.allProductos];

    if (this.categoriaActiva !== 'Todos') {
      result = result.filter(p => p.categoria === this.categoriaActiva);
    }
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(p =>
        p.nombre.toLowerCase().includes(q) || p.descripcion?.toLowerCase().includes(q)
      );
    }
    if (this.mostrarSoloFavoritos) {
      result = result.filter(p => this.favoritosIds.has(p.id));
    }

    switch (this.ordenActivo) {
      case 'precio_asc':  result.sort((a, b) => a.precio - b.precio); break;
      case 'precio_desc': result.sort((a, b) => b.precio - a.precio); break;
      case 'destacado':   result.sort((a, b) => (b.destacado ? 1 : 0) - (a.destacado ? 1 : 0)); break;
      default:            result.sort((a, b) => a.nombre.localeCompare(b.nombre));
    }

    this.productosFiltrados = result;
    this.cdr.detectChanges();
  }

  toggleFavorito(producto: Producto): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.favoritoService.toggle(user.id, producto.id).subscribe({
      next: (res: any) => {
        if (res?.accion === 'eliminado') {
          this.favoritosIds.delete(producto.id);
        } else {
          this.favoritosIds.add(producto.id);
        }
        if (this.mostrarSoloFavoritos) this.aplicarFiltros();
        this.cdr.detectChanges();
      }
    });
  }

  esFavorito(id: number): boolean {
    return this.favoritosIds.has(id);
  }

  agregarAlCarrito(producto: Producto): void {
    const item = this.carrito.find(c => c.producto.id === producto.id);
    if (item) {
      item.cantidad++;
    } else {
      this.carrito.push({ producto, cantidad: 1 });
    }
    this.carritoAbierto = true;
    this.cdr.detectChanges();
  }

  cambiarCantidad(item: { producto: Producto; cantidad: number }, delta: number): void {
    item.cantidad += delta;
    if (item.cantidad <= 0) {
      this.carrito = this.carrito.filter(c => c.producto.id !== item.producto.id);
    }
    this.cdr.detectChanges();
  }

  get totalCarrito(): number {
    return this.carrito.reduce((s, c) => s + this.precioFinal(c.producto) * c.cantidad, 0);
  }

  get cantidadCarrito(): number {
    return this.carrito.reduce((s, c) => s + c.cantidad, 0);
  }

  isLoggedIn(): boolean {
    return !!this.authService.getCurrentUser();
  }

  tieneDescuento(): boolean {
    return this.isLoggedIn();
  }

  precioFinal(producto: Producto): number {
    return this.tieneDescuento() ? producto.precio * 0.9 : producto.precio;
  }

  // ── Checkout ─────────────────────────────────────────────────────────────

  abrirCheckout(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.checkoutEmail = user.email ?? '';
      this.checkoutNombre = user.email ?? '';
      this.checkoutStep = 'pago';
      this._iniciarPagoStripe();
    } else {
      this.checkoutEmail = '';
      this.checkoutNombre = '';
      this.checkoutStep = 'contacto';
    }
    this.checkoutError = '';
    this.checkoutEmailError = '';
    this.checkoutStripeError = '';
    this.checkoutStripeReady = false;
    this.carritoAbierto = false;
    this.checkoutAbierto = true;
    this.cdr.detectChanges();
  }

  async continuarACheckout(): Promise<void> {
    if (!this.checkoutEmail || !this.checkoutEmail.includes('@')) {
      this.checkoutEmailError = 'Introduce un email válido.';
      return;
    }
    if (!this.checkoutNombre.trim()) {
      this.checkoutEmailError = 'Introduce tu nombre.';
      return;
    }
    this.checkoutEmailError = '';
    this.checkoutStep = 'pago';
    this.cdr.detectChanges();
    await this._iniciarPagoStripe();
  }

  private _iniciarPagoStripe(): void {
    this.checkoutIsLoading = true;
    this.checkoutStripeReady = false;
    this.checkoutStripeError = '';
    this.cdr.detectChanges();

    const amountCents = Math.round(this.totalCarrito * 100);
    this.stripeService.createOrderPaymentIntent(amountCents, this.checkoutEmail).subscribe({
      next: async (res) => {
        const clientSecret = res.clientSecret;
        await new Promise(resolve => setTimeout(resolve, 80));
        try {
          this.checkoutStripe = await this.stripeService.getStripe();
          this.checkoutElements = this.checkoutStripe.elements({ clientSecret, locale: 'es' });
          this.checkoutPaymentElement = this.checkoutElements.create('payment');

          const container = document.getElementById('checkout-payment-element');
          if (!container) {
            this.checkoutStripeError = 'Error al cargar el formulario de pago.';
            this.checkoutIsLoading = false;
            this.cdr.detectChanges();
            return;
          }

          this.checkoutPaymentElement.on('ready', () => {
            this.checkoutStripeReady = true;
            this.checkoutIsLoading = false;
            this.cdr.detectChanges();
          });

          this.checkoutPaymentElement.on('loaderror', (e: any) => {
            this.checkoutStripeError = 'Error al cargar el pago: ' + (e?.error?.message ?? '');
            this.checkoutIsLoading = false;
            this.cdr.detectChanges();
          });

          this.checkoutPaymentElement.mount(container);
        } catch {
          this.checkoutStripeError = 'Error al conectar con Stripe.';
          this.checkoutIsLoading = false;
          this.cdr.detectChanges();
        }
      },
      error: () => {
        this.checkoutStripeError = 'Error al iniciar el pago. Inténtalo más tarde.';
        this.checkoutIsLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  async confirmarPagoCheckout(): Promise<void> {
    if (!this.checkoutStripe || !this.checkoutElements) return;
    this.checkoutIsLoading = true;
    this.checkoutError = '';
    this.cdr.detectChanges();

    const result = await this.checkoutStripe.confirmPayment({
      elements: this.checkoutElements,
      confirmParams: { return_url: window.location.href },
      redirect: 'if_required'
    });

    if (result.error) {
      this.checkoutError = result.error.message || 'Error al procesar el pago.';
      this.checkoutIsLoading = false;
      this.cdr.detectChanges();
      return;
    }

    // Pago OK → enviar email de confirmación
    const items = this.carrito.map(c => ({
      nombre: c.producto.nombre,
      cantidad: c.cantidad,
      precioUnitario: this.precioFinal(c.producto)
    }));

    this.stripeService.confirmarPedido(
      this.checkoutEmail,
      this.checkoutNombre,
      items,
      this.totalCarrito
    ).subscribe({
      next: () => {
        this.checkoutStep = 'confirmacion';
        this.checkoutIsLoading = false;
        this.carrito = [];
        this.cdr.detectChanges();
      },
      error: () => {
        // Email falló pero pago OK → mostrar confirmación de todas formas
        this.checkoutStep = 'confirmacion';
        this.checkoutIsLoading = false;
        this.carrito = [];
        this.cdr.detectChanges();
      }
    });
  }

  cerrarCheckout(): void {
    if (this.checkoutPaymentElement) {
      this.checkoutPaymentElement.destroy();
      this.checkoutPaymentElement = null;
    }
    this.checkoutAbierto = false;
    this.checkoutStripe = null;
    this.checkoutElements = null;
    this.checkoutStripeReady = false;
    this.checkoutStep = 'contacto';
    this.cdr.detectChanges();
  }
}
