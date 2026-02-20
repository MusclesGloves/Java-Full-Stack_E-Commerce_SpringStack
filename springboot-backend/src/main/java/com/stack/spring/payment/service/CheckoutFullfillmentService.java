package com.stack.spring.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stack.spring.model.Product;
import com.stack.spring.payment.model.OrderPayment;
import com.stack.spring.payment.repo.OrderPaymentRepository;
import com.stack.spring.payment.dto.CheckoutItem;
import com.stack.spring.repo.ProductRepo;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckoutFullfillmentService {

    private final OrderPaymentRepository orderPaymentRepository;
    private final ProductRepo productRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CheckoutFullfillmentService(OrderPaymentRepository orderPaymentRepository, ProductRepo productRepo) {
        this.orderPaymentRepository = orderPaymentRepository;
        this.productRepo = productRepo;
    }

    /**
     * Fulfill = decrement stock exactly once after payment is PAID.
     * Idempotent: if already fulfilled, it does nothing (safe on retries).
     */
    @Transactional
    public void fulfill(String orderId, String username) {
        if (orderId == null || orderId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing orderId");
        }
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        OrderPayment op = orderPaymentRepository.findByOrderId(orderId);
        if (op == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order not found");
        }
        if (!username.equals(op.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        // Must be PAID before fulfillment
        if (op.getStatus() != OrderPayment.Status.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not PAID");
        }

        // Idempotency: never decrement twice
        if (op.isFulfilled()) {
            return; // safe retry
        }

        String itemsJson = op.getCheckoutItemsJson();
        if (itemsJson == null || itemsJson.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No checkout items attached to this payment");
        }

        // Parse items from JSON
        List<CheckoutItem> items;
        try {
            items = objectMapper.readValue(itemsJson, new TypeReference<List<CheckoutItem>>() {});
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid checkout items JSON");
        }

        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No items to fulfill");
        }

        // Aggregate quantities by productId (handles duplicates safely)
        Map<Integer, Integer> qtyByProductId = new HashMap<>();
        for (CheckoutItem it : items) {
            if (it.getProductId() == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid productId in items");
            }
            if (it.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity for product: " + it.getProductId());
            }
            qtyByProductId.merge(it.getProductId(), it.getQuantity(), Integer::sum);
        }

        // Batch fetch products
        List<Integer> productIds = qtyByProductId.keySet().stream().toList();
        Map<Integer, Product> productMap = new HashMap<>();
        for (Product p : productRepo.findAllById(productIds)) {
            productMap.put(p.getId(), p);
        }

        // Validate stock
        for (Map.Entry<Integer, Integer> entry : qtyByProductId.entrySet()) {
            Integer productId = entry.getKey();
            int qtyNeeded = entry.getValue();

            Product p = productMap.get(productId);
            if (p == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId);
            }
            if (p.getStockQuantity() < qtyNeeded) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product: " + p.getName());
            }
        }

        // Decrement stock
        for (Map.Entry<Integer, Integer> entry : qtyByProductId.entrySet()) {
            Integer productId = entry.getKey();
            int qtyNeeded = entry.getValue();

            Product p = productMap.get(productId);
            int newStock = p.getStockQuantity() - qtyNeeded;
            p.setStockQuantity(Math.max(newStock, 0));

            if (p.getStockQuantity() == 0) {
                p.setProductAvailable(false);
            }
        }

        productRepo.saveAll(productMap.values());

        // Mark fulfilled (idempotency flag)
        op.setFulfilled(true);
        orderPaymentRepository.save(op);
    }
}
