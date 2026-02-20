package com.telusko.springecom.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String provider;
    private String orderId;
    private String paymentId;

    private BigDecimal amount;
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    private Status status = Status.CREATED;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public enum Status { CREATED, PAID, FAILED }

    @PreUpdate
    public void touch(){ this.updatedAt = Instant.now(); }

}
