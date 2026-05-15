import { Component, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SocioService } from '../core/services/socio.service';
import { StripeService } from '../core/services/stripe.service';

/** Pasos del formulario */
type Step = 'datos' | 'pago' | 'confirmacion';

@Component({
  selector: 'app-socio-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './socio-form.html'
})
export class SocioFormComponent implements AfterViewInit {
  step: Step = 'datos';
  socioForm: FormGroup;
  idSolicitud = '';
  isLoading = false;
  errorMessage = '';

  // Stripe
  private stripe: any = null;
  private elements: any = null;
  private paymentElement: any = null;
  stripeReady = false;
  stripeError = '';

  // Precio cuota anual en céntimos (€30,00)
  readonly CUOTA_CENTS = 3000;
  readonly CUOTA_EUR   = (this.CUOTA_CENTS / 100).toFixed(2);

  private clientSecret = '';

  constructor(
    private fb: FormBuilder,
    private socioService: SocioService,
    private stripeService: StripeService,
    private cdr: ChangeDetectorRef
  ) {
    this.socioForm = this.fb.group({
      nombre:     ['', Validators.required],
      apellidos:  ['', Validators.required],
      dni:        ['', [Validators.required, Validators.minLength(9)]],
      telefono:   ['', Validators.required],
      email:      ['', [Validators.required, Validators.email]],
      privacidad: [false, Validators.requiredTrue]
    });
  }

  ngAfterViewInit(): void {}

  /** Paso 1 → ir a pago */
  async irAPago(): Promise<void> {
    if (this.socioForm.invalid) return;
    this.isLoading = true;
    this.errorMessage = '';
    try {
      this.stripeService.createPaymentIntent(this.CUOTA_CENTS).subscribe({
        next: async (res) => {
          this.clientSecret = res.clientSecret;
          this.step = 'pago';
          this.cdr.detectChanges();
          // Esperar al siguiente ciclo del DOM para que el div#stripe-payment-element exista
          await new Promise(resolve => setTimeout(resolve, 50));
          await this.mountStripeElement();
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Error al iniciar el pago. Inténtalo más tarde.';
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    } catch {
      this.errorMessage = 'Error al conectar con Stripe.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  private async mountStripeElement(): Promise<void> {
    try {
      this.stripe = await this.stripeService.getStripe();
      this.elements = this.stripe.elements({ clientSecret: this.clientSecret, locale: 'es' });
      this.paymentElement = this.elements.create('payment');

      const container = document.getElementById('stripe-payment-element');
      if (!container) {
        this.stripeError = 'No se pudo cargar el formulario de pago (contenedor no encontrado).';
        this.cdr.detectChanges();
        return;
      }

      this.paymentElement.on('ready', () => {
        this.stripeReady = true;
        this.cdr.detectChanges();
      });

      this.paymentElement.on('loaderror', (event: any) => {
        this.stripeError = 'Error al cargar el formulario de pago: ' + (event?.error?.message ?? '');
        this.cdr.detectChanges();
      });

      this.paymentElement.mount(container);
    } catch (e: any) {
      this.stripeError = 'Error al cargar el formulario de pago.';
      this.cdr.detectChanges();
    }
  }

  /** Paso 2 → confirmar pago y registrar socio */
  async confirmarPago(): Promise<void> {
    if (!this.stripe || !this.elements) return;
    this.isLoading = true;
    this.errorMessage = '';

    const result = await this.stripe.confirmPayment({
      elements: this.elements,
      confirmParams: { return_url: window.location.href },
      redirect: 'if_required'
    });

    if (result.error) {
      this.errorMessage = result.error.message || 'Error al procesar el pago.';
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    // Pago completado → guardar socio
    const { privacidad, ...datos } = this.socioForm.value;
    this.socioService.create(datos).subscribe({
      next: (socio) => {
        this.idSolicitud = `ALB-${socio.id.toString().padStart(4, '0')}`;
        this.step = 'confirmacion';
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.status === 400
          ? 'Ya existe un socio con ese DNI.'
          : 'Pago completado pero error al guardar los datos. Contacta con el club.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  volverADatos(): void {
    this.step = 'datos';
    this.stripeReady = false;
    this.stripeError = '';
    this.errorMessage = '';
    if (this.paymentElement) {
      this.paymentElement.destroy();
      this.paymentElement = null;
    }
  }
}
