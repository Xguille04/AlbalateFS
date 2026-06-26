import { Component, AfterViewInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SocioService } from '../core/services/socio.service';
import { StripeService } from '../core/services/stripe.service';
import { StripeInstance, StripeElements, StripeElement, StripeElementChangeEvent } from '../core/models/stripe.models';

/** Pasos del formulario */
type Step = 'datos' | 'pago' | 'confirmacion';

@Component({
  selector: 'app-socio-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './socio-form.html'
})
export class SocioFormComponent implements AfterViewInit {
  step = signal<Step>('datos');
  socioForm: FormGroup;
  idSolicitud = signal('');
  isLoading = signal(false);
  errorMessage = signal('');

  // Stripe (signals para reactivity)
  private stripe: StripeInstance | null = null;
  private elements: StripeElements | null = null;
  private paymentElement: StripeElement | null = null;
  stripeReady = signal(false);
  stripeError = signal('');

  // Precio cuota anual en céntimos (€30,00)
  readonly CUOTA_CENTS = 3000;
  readonly CUOTA_EUR   = (this.CUOTA_CENTS / 100).toFixed(2);

  private clientSecret = '';

  constructor(
    private fb: FormBuilder,
    private socioService: SocioService,
    private stripeService: StripeService
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
    this.isLoading.set(true);
    this.errorMessage.set('');
    try {
      this.stripeService.createPaymentIntent(this.CUOTA_CENTS).subscribe({
        next: async (res) => {
          this.clientSecret = res.clientSecret;
          this.step.set('pago');
          // Esperar al siguiente ciclo del DOM para que el div#stripe-payment-element exista
          await new Promise(resolve => setTimeout(resolve, 50));
          await this.mountStripeElement();
          this.isLoading.set(false);
        },
        error: () => {
          this.errorMessage.set('Error al iniciar el pago. Inténtalo más tarde.');
          this.isLoading.set(false);
        }
      });
    } catch {
      this.errorMessage.set('Error al conectar con Stripe.');
      this.isLoading.set(false);
    }
  }

  private async mountStripeElement(): Promise<void> {
    try {
      this.stripe = await this.stripeService.getStripe();
      this.elements = this.stripe.elements({ clientSecret: this.clientSecret, locale: 'es' });
      this.paymentElement = this.elements.create('payment');

      const container = document.getElementById('stripe-payment-element');
      if (!container) {
        this.stripeError.set('No se pudo cargar el formulario de pago (contenedor no encontrado).');
        return;
      }

      this.paymentElement.on('ready', () => {
        this.stripeReady.set(true);
      });

      this.paymentElement.on('loaderror', (event: StripeElementChangeEvent) => {
        const message = event.error?.message ?? 'Error desconocido';
        this.stripeError.set(`Error al cargar el formulario de pago: ${message}`);
      });

      this.paymentElement.mount(container);
    } catch (e) {
      this.stripeError.set('Error al cargar el formulario de pago.');
    }
  }

  /** Paso 2 → confirmar pago y registrar socio */
  async confirmarPago(): Promise<void> {
    if (!this.stripe || !this.elements) return;
    this.isLoading.set(true);
    this.errorMessage.set('');

    const result = await this.stripe.confirmPayment({
      elements: this.elements,
      confirmParams: { return_url: window.location.href },
      redirect: 'if_required'
    });

    if (result.error) {
      this.errorMessage.set(result.error.message || 'Error al procesar el pago.');
      this.isLoading.set(false);
      return;
    }

    // Pago completado → guardar socio
    const { privacidad, ...datos } = this.socioForm.value;
    this.socioService.create(datos).subscribe({
      next: (socio) => {
        this.idSolicitud.set(`ALB-${socio.id.toString().padStart(4, '0')}`);
        this.step.set('confirmacion');
        this.isLoading.set(false);
      },
      error: (err) => {
        const message = err.status === 400
          ? 'Ya existe un socio con ese DNI.'
          : 'Pago completado pero error al guardar los datos. Contacta con el club.';
        this.errorMessage.set(message);
        this.isLoading.set(false);
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
