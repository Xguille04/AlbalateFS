package com.albalatefs.backend.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class PaymentIntentRequest {
    @Min(value = 50, message = "El importe minimo es 50 centimos")
    private long amountCents;

    @Pattern(regexp = "^[a-zA-Z]{3}$", message = "La moneda debe tener 3 letras")
    private String currency;

    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
