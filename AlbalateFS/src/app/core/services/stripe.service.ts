import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

declare var Stripe: any;

@Injectable({ providedIn: 'root' })
export class StripeService {
  private readonly apiUrl = 'http://localhost:8080/api/stripe';
  private stripeInstance: any = null;

  constructor(private http: HttpClient) {}

  /** Load (or return cached) Stripe instance using key from backend */
  async getStripe(): Promise<any> {
    if (this.stripeInstance) return this.stripeInstance;
    const { publishableKey } = await this.http
      .get<{ publishableKey: string }>(`${this.apiUrl}/config`)
      .toPromise() as { publishableKey: string };
    this.stripeInstance = (window as any).Stripe(publishableKey);
    return this.stripeInstance;
  }

  createPaymentIntent(amountCents: number): Observable<{ clientSecret: string }> {
    return this.http.post<{ clientSecret: string }>(
      `${this.apiUrl}/create-payment-intent`,
      { amountCents, currency: 'eur' }
    );
  }

  createOrderPaymentIntent(amountCents: number, email: string): Observable<{ clientSecret: string }> {
    return this.http.post<{ clientSecret: string }>(
      `${this.apiUrl}/create-order-payment-intent`,
      { amountCents, email, currency: 'eur' }
    );
  }

  confirmarPedido(
    email: string,
    nombre: string,
    items: Array<{ nombre: string; cantidad: number; precioUnitario: number }>,
    totalEur: number
  ): Observable<any> {
    return this.http.post(`${this.apiUrl}/confirmar-pedido`, {
      email,
      nombre,
      amountCents: Math.round(totalEur * 100),
      items
    });
  }
}
