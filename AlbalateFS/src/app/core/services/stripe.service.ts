import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StripeInstance, PaymentIntentResponse } from '../models/stripe.models';

declare var Stripe: (publishableKey: string) => StripeInstance;

@Injectable({ providedIn: 'root' })
export class StripeService {
  private readonly apiUrl = `${environment.apiUrl}/stripe`;
  private stripeInstance: StripeInstance | null = null;

  constructor(private http: HttpClient) {}

  /** Load (or return cached) Stripe instance using key from backend */
  async getStripe(): Promise<StripeInstance> {
    if (this.stripeInstance) return this.stripeInstance;
    const { publishableKey } = await this.http
      .get<{ publishableKey: string }>(`${this.apiUrl}/config`)
      .toPromise() as { publishableKey: string };
    this.stripeInstance = Stripe(publishableKey);
    return this.stripeInstance;
  }

  createPaymentIntent(amountCents: number): Observable<PaymentIntentResponse> {
    return this.http.post<PaymentIntentResponse>(
      `${this.apiUrl}/create-payment-intent`,
      { amountCents, currency: 'eur' }
    );
  }

  createOrderPaymentIntent(amountCents: number, email: string): Observable<PaymentIntentResponse> {
    return this.http.post<PaymentIntentResponse>(
      `${this.apiUrl}/create-order-payment-intent`,
      { amountCents, email, currency: 'eur' }
    );
  }

  confirmarPedido(
    email: string,
    nombre: string,
    items: Array<{ nombre: string; cantidad: number; precioUnitario: number }>,
    totalEur: number
  ): Observable<{ ok: boolean }> {
    return this.http.post<{ ok: boolean }>(`${this.apiUrl}/confirmar-pedido`, {
      email,
      nombre,
      amountCents: Math.round(totalEur * 100),
      items
    });
  }
}
