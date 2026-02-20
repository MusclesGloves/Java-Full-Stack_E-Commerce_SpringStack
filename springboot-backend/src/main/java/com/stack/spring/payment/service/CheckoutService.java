package com.stack.spring.payment.service;

import com.stack.spring.model.Product;
import com.stack.spring.payment.model.OrderPayment;
import com.stack.spring.payment.repo.OrderPaymentRepository;
import com.stack.spring.payment.dto.CheckoutItem;
import com.stack.spring.payment.dto.CheckoutRequest;
import com.stack.spring.repo.ProductRepo;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckoutService {

    private final ProductRepo productRepo;
    private final OrderPaymentRepository orderPaymentRepository;

    public CheckoutService(ProductRepo productRepo, OrderPaymentRepository orderPaymentRepository) {
        this.productRepo = productRepo;
        this.orderPaymentRepository = orderPaymentRepository;
    }

    @Transactional
    public Map<String, Object> checkout(CheckoutRequest req, Principal principal) {

        // 1) Basic request validation
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No items");
        }
        BigDecimal computedAmount = BigDecimal.ZERO;

        List<Integer> productIds = req.getItems()
                .stream()
                .map(CheckoutItem::getProductId)
                .distinct()
                .toList();

        Map<Integer, Product> productMap = new HashMap<>();
        for (Product p : productRepo.findAllById(productIds)) {
            productMap.put(p.getId(), p);
        }



        // 2) (Optional) verify stock availability before committing
        for (CheckoutItem it : req.getItems()) {
            Product p = productMap.get(it.getProductId());

            if (p == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found: " + it.getProductId()
                );
            }

            if (it.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid quantity for product: " + it.getProductId()
                );
            }

            if (p.getStockQuantity() < it.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for product: " + p.getName()
                );
            }
            computedAmount = computedAmount.add(
                    p.getPrice().multiply(BigDecimal.valueOf(it.getQuantity()))
            );
        }

        // 3) Record mock payment row (replace with real gateway verify later)
        OrderPayment op = new OrderPayment();
        op.setUsername(principal != null ? principal.getName() : "guest");
        op.setProvider("mock");
        op.setOrderId("MOCK-" + System.currentTimeMillis());
        op.setPaymentId("MOCK-PAY-" + System.nanoTime());
        op.setStatus(OrderPayment.Status.PAID);
        op.setAmount(computedAmount);
        op.setCreatedAt(Instant.now());
        orderPaymentRepository.save(op);

        // 4) Decrement stock for each item
        for (CheckoutItem it : req.getItems()) {
            Product p = productMap.get(it.getProductId());

            int newStock = p.getStockQuantity() - it.getQuantity();
            p.setStockQuantity(Math.max(newStock, 0));

            if (p.getStockQuantity() == 0) {
                p.setProductAvailable(false);
            }
        }

        productRepo.saveAll(productMap.values());

        // 5) Success payload
        Map<String, Object> body = new HashMap<>();
        body.put("amount", computedAmount);
        body.put("status", "PAID");
        body.put("orderId", op.getOrderId());
        body.put("paymentId", op.getPaymentId());
        return body;

    }

}
