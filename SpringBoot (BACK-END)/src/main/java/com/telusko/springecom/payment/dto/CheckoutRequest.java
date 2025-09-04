package com.telusko.springecom.payment.dto;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutRequest {
    private BigDecimal amount;
    private List<CheckoutItem> items;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public List<CheckoutItem> getItems() { return items; }
    public void setItems(List<CheckoutItem> items) { this.items = items; }
}
