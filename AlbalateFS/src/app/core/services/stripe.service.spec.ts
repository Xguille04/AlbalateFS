import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { StripeService } from './stripe.service';

describe('StripeService', () => {
  let service: StripeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [StripeService]
    });
    service = TestBed.inject(StripeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create payment intent for membership', (done) => {
    const amountCents = 3000;
    const response = { clientSecret: 'test_secret_123' };

    service.createPaymentIntent(amountCents).subscribe((result) => {
      expect(result.clientSecret).toBe('test_secret_123');
      done();
    });

    const req = httpMock.expectOne(req => req.url.includes('/api/stripe/create-payment-intent'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body.amountCents).toBe(3000);
    req.flush(response);
  });

  it('should create order payment intent', (done) => {
    const amountCents = 5000;
    const email = 'test@example.com';
    const response = { clientSecret: 'order_secret_456' };

    service.createOrderPaymentIntent(amountCents, email).subscribe((result) => {
      expect(result.clientSecret).toBe('order_secret_456');
      done();
    });

    const req = httpMock.expectOne(req => req.url.includes('/api/stripe/create-order-payment-intent'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body.email).toBe('test@example.com');
    req.flush(response);
  });

  it('should send order confirmation', (done) => {
    const email = 'buyer@example.com';
    const nombre = 'Juan';
    const items = [{ nombre: 'Jersey', cantidad: 1, precioUnitario: 30 }];
    const totalEur = 30;

    service.confirmarPedido(email, nombre, items, totalEur).subscribe((result) => {
      expect(result.ok).toBe(true);
      done();
    });

    const req = httpMock.expectOne(req => req.url.includes('/api/stripe/confirmar-pedido'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body.amountCents).toBe(3000);
    req.flush({ ok: true });
  });
});
