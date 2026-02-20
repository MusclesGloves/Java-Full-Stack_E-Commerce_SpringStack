package com.stack.spring.payment.service;

import com.stack.spring.model.Product;
import com.stack.spring.payment.dto.CheckoutItem;
import com.stack.spring.payment.dto.CheckoutRequest;
import com.stack.spring.repo.ProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckoutPaymentService {

    private final ProductRepo productRepo;
    private final PaymentService paymentService;

    public CheckoutPaymentService(ProductRepo productRepo, PaymentService paymentService) {
        this.productRepo = productRepo;
        this.paymentService = paymentService;
    }

    /**
     * STEP 5 CORE:
     * Client sends items only.
     * Server fetches product prices from DB, validates stock, computes amount.
     * Then calls PaymentService.createOrder(computedAmount, username).
     */
    public Map<String, Object> createOrderFromCheckout(CheckoutRequest req, String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No items");
        }

        // 1) Collect distinct product IDs
        List<Integer> productIds = req.getItems().stream()
                .map(CheckoutItem::getProductId)
                .distinct()
                .toList();

        // 2) Fetch products from DB in one shot
        Map<Integer, Product> productMap = new HashMap<>();
        for (Product p : productRepo.findAllById(productIds)) {
            productMap.put(p.getId(), p);
        }

        // 3) Validate + compute amount from DB prices (NOT from client)
        BigDecimal computedAmount = BigDecimal.ZERO;

        for (CheckoutItem it : req.getItems()) {
            Product p = productMap.get(it.getProductId());

            if (p == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + it.getProductId());
            }
            if (it.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity for product: " + it.getProductId());
            }
            if (p.getStockQuantity() < it.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product: " + p.getName());
            }
            if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product price for: " + p.getName());
            }

            computedAmount = computedAmount.add(
                    p.getPrice().multiply(BigDecimal.valueOf(it.getQuantity()))
            );
        }

        // 4) Create Razorpay/mock order using computedAmount
        Map<String, Object> paymentResp = paymentService.createOrder(computedAmount, username);

        // 5) Return both: computedAmount + payment info (helps frontend)
        Map<String, Object> resp = new HashMap<>(paymentResp);
        resp.put("computedAmount", computedAmount);
        return resp;
    }
}
