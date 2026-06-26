/**
 * Modelos para Stripe y elementos de pago.
 * Reemplaza el uso de `any` con tipos específicos.
 */

export interface StripeInstance {
  elements(options: ElementsCreateOptions): StripeElements;
  confirmPayment(options: ConfirmPaymentOptions): Promise<ConfirmPaymentResult>;
  retrievePaymentIntent(clientSecret: string): Promise<PaymentIntentRetrieveResult>;
}

export interface ElementsCreateOptions {
  clientSecret: string;
  locale?: string;
}

export interface StripeElements {
  create(type: string, options?: any): StripeElement;
}

export interface StripeElement {
  mount(element: string | HTMLElement): void;
  unmount(): void;
  on(event: string, handler: (event: StripeElementChangeEvent) => void): void;
  blur(): void;
  focus(): void;
  clear(): void;
}

export interface StripeElementChangeEvent {
  complete: boolean;
  empty: boolean;
  brand?: string;
  error?: StripeError;
}

export interface StripeError {
  type: string;
  code: string;
  message: string;
  param?: string;
}

export interface ConfirmPaymentOptions {
  elements?: StripeElements;
  confirmParams: {
    return_url: string;
  };
  redirect?: 'if_required' | 'always';
}

export interface ConfirmPaymentResult {
  paymentIntent?: PaymentIntent;
  error?: StripeError;
}

export interface PaymentIntentRetrieveResult {
  paymentIntent?: PaymentIntent;
  error?: StripeError;
}

export interface PaymentIntent {
  id: string;
  object: string;
  status: 'requires_payment_method' | 'requires_confirmation' | 'requires_action' | 'processing' | 'requires_capture' | 'canceled' | 'succeeded';
  client_secret: string;
  amount: number;
  currency: string;
  created: number;
  customer?: string;
  metadata?: Record<string, any>;
}

export interface PaymentIntentResponse {
  clientSecret: string;
}
