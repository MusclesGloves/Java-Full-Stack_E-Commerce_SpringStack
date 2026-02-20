package com.stack.spring.payment.controller;

import com.stack.spring.payment.model.OrderPayment;
import com.stack.spring.payment.dto.CheckoutRequest;
import com.stack.spring.payment.service.CheckoutPaymentService;
import com.stack.spring.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final CheckoutPaymentService checkoutPaymentService;

    public PaymentController(PaymentService paymentService,
                             CheckoutPaymentService checkoutPaymentService) {
        this.paymentService = paymentService;
        this.checkoutPaymentService = checkoutPaymentService;
    }

    /**
     * STEP 5 (Production-safe): create order from cart items.
     * Client sends items. Server computes amount from DB. Then creates Razorpay/mock order.
     */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/checkout/create-order")
    public ResponseEntity<?> createOrderFromCheckout(@RequestBody CheckoutRequest req, Authentication auth) {
        return ResponseEntity.ok(checkoutPaymentService.createOrderFromCheckout(req, auth.getName()));
    }

    /**
     * Optional: keep this ONLY for debugging.
     * In real production flow, you should prefer /checkout/create-order.
     */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body, Authentication auth) {
        BigDecimal amountRs = new BigDecimal(String.valueOf(body.getOrDefault("amount", "1")));
        return ResponseEntity.ok(paymentService.createOrder(amountRs, auth.getName()));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body, Authentication auth) {
        return ResponseEntity.ok(paymentService.verify(body, auth.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<OrderPayment> allOrders() {
        return paymentService.allOrders();
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/my")
    public List<OrderPayment> myOrders(Authentication auth) {
        return paymentService.myOrders(auth.getName());
    }
}
