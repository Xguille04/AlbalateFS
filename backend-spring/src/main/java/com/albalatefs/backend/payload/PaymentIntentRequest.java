package com.albalatefs.backend.payload;

public class PaymentIntentRequest {
    private long amountCents;
    private String currency;

    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
