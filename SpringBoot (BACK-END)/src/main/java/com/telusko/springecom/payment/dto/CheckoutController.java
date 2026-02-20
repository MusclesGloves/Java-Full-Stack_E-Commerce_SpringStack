package com.telusko.springecom.payment.dto;

import com.telusko.springecom.model.Product;
import com.telusko.springecom.payment.dto.CheckoutItem;
import com.telusko.springecom.payment.dto.CheckoutRequest;
import com.telusko.springecom.payment.OrderPayment;
import com.telusko.springecom.payment.OrderPaymentRepository;
import com.telusko.springecom.repo.ProductRepo;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class CheckoutController {

    private final ProductRepo ProductRepo;
    private final OrderPaymentRepository orderPaymentRepository;

    public CheckoutController(ProductRepo ProductRepo,
                              OrderPaymentRepository orderPaymentRepository) {
        this.ProductRepo = ProductRepo;
        this.orderPaymentRepository = orderPaymentRepository;
    }

    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest req, Principal principal) {
        // 1) Basic request validation
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No items"));
        }
        BigDecimal amount = req.getAmount() == null ? BigDecimal.ZERO : req.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount"));
        }

        // 2) (Optional) verify stock availability before committing
        for (CheckoutItem it : req.getItems()) {
            Product p = ProductRepo.findById(it.getProductId()).orElse(null);
            if (p == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found: " + it.getProductId()));
            }
            if (it.getQuantity() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid quantity for product: " + it.getProductId()));
            }
            if (p.getStockQuantity() < it.getQuantity()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Insufficient stock for product: " + p.getName()));
            }
        }

        // 3) Record mock payment row (replace with real gateway verify later)
        OrderPayment op = new OrderPayment();
        op.setUsername(principal != null ? principal.getName() : "guest");
        op.setProvider("mock");
        op.setOrderId("MOCK-" + System.currentTimeMillis());
        op.setPaymentId("MOCK-PAY-" + System.nanoTime());
        op.setStatus(OrderPayment.Status.PAID);
        op.setAmount(amount);
        op.setCreatedAt(Instant.now());
        orderPaymentRepository.save(op);

        // 4) Decrement stock for each item
        for (CheckoutItem it : req.getItems()) {
            Product p = ProductRepo.findById(it.getProductId()).orElseThrow();
            p.setStockQuantity(p.getStockQuantity() - it.getQuantity());
            // Also toggle availability if it hits 0
            if (p.getStockQuantity() <= 0) {
                p.setStockQuantity(0);
                p.setProductAvailable(false);
            }
            ProductRepo.save(p);
        }

        // 5) Success payload
        Map<String, Object> body = new HashMap<>();
        body.put("status", "PAID");
        body.put("orderId", op.getOrderId());
        body.put("paymentId", op.getPaymentId());
        return ResponseEntity.ok(body);
    }
}
