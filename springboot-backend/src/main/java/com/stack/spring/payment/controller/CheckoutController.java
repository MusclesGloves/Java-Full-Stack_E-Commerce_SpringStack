package com.stack.spring.payment.controller;

import com.stack.spring.payment.service.CheckoutService;
import com.stack.spring.payment.repo.OrderPaymentRepository;
import com.stack.spring.payment.dto.CheckoutRequest;
import com.stack.spring.repo.ProductRepo;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/payments")
public class CheckoutController {

    private final ProductRepo ProductRepo;
    private final OrderPaymentRepository orderPaymentRepository;
    private final CheckoutService checkoutService;

    public CheckoutController(ProductRepo ProductRepo,
                              OrderPaymentRepository orderPaymentRepository, CheckoutService checkoutService) {
        this.ProductRepo = ProductRepo;
        this.orderPaymentRepository = orderPaymentRepository;
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest req, Principal principal) {
        return ResponseEntity.ok(checkoutService.checkout(req, principal));
    }
}
