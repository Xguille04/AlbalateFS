package com.albalatefs.backend.controller;

import com.albalatefs.backend.payload.PaymentIntentRequest;
import com.albalatefs.backend.payload.PedidoRequest;
import com.albalatefs.backend.service.EmailService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@CrossOrigin(origins = "*")
public class StripeController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @Autowired
    private EmailService emailService;

    /**
     * Returns the publishable key so the frontend can initialise Stripe.js
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(Map.of("publishableKey", stripePublishableKey));
    }

    /**
     * Creates a PaymentIntent for the membership fee.
     */
    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentIntentRequest request) {
        try {
            Stripe.apiKey = stripeSecretKey;

            long amount = request.getAmountCents() > 0 ? request.getAmountCents() : 3000;
            String currency = request.getCurrency() != null ? request.getCurrency() : "eur";

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .setDescription("Alta de socio - Albalate FS")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return ResponseEntity.ok(Map.of("clientSecret", intent.getClientSecret()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Creates a PaymentIntent for a tienda order.
     */
    @PostMapping("/create-order-payment-intent")
    public ResponseEntity<?> createOrderPaymentIntent(@RequestBody PedidoRequest request) {
        try {
            Stripe.apiKey = stripeSecretKey;

            long amount = request.getAmountCents() > 0 ? request.getAmountCents() : 100;

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("eur")
                    .setDescription("Pedido tienda - Albalate FS")
                    .setReceiptEmail(request.getEmail())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return ResponseEntity.ok(Map.of("clientSecret", intent.getClientSecret()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Sends order confirmation email after successful payment.
     */
    @PostMapping("/confirmar-pedido")
    public ResponseEntity<?> confirmarPedido(@RequestBody PedidoRequest request) {
        try {
            double totalEur = request.getAmountCents() / 100.0;
            emailService.enviarConfirmacionPedido(
                    request.getEmail(),
                    request.getNombre(),
                    request.getItems(),
                    totalEur
            );
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
