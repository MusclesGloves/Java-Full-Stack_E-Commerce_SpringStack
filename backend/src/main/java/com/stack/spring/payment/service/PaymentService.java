package com.stack.spring.payment.service;

import com.stack.spring.payment.model.OrderPayment;
import com.stack.spring.payment.repo.OrderPaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${payments.provider:mock}")
    private String provider; // razorpay | mock

    @Value("${razorpay.keyId:}")
    private String razorKeyId;

    @Value("${razorpay.keySecret:}")
    private String razorKeySecret;

    private final CheckoutFullfillmentService checkoutFulfillmentService;

    private final OrderPaymentRepository repo;

    public PaymentService(CheckoutFullfillmentService checkoutFulfillmentService, OrderPaymentRepository repo) {
        this.checkoutFulfillmentService = checkoutFulfillmentService;
        this.repo = repo;
    }

    public Map<String, Object> createOrder(BigDecimal amountRs, String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (amountRs == null || amountRs.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }

        BigDecimal amountPaise = amountRs.multiply(new BigDecimal("100"));

        OrderPayment op = new OrderPayment();
        op.setUsername(username);
        op.setAmount(amountRs);
        op.setProvider(provider);

        // MOCK path (default)
        if (!"razorpay".equalsIgnoreCase(provider) || isBlank(razorKeyId) || isBlank(razorKeySecret)) {
            op.setOrderId("order_mock_" + System.currentTimeMillis());
            op.setPaymentId("pay_mock_" + System.nanoTime());
            op.setStatus(OrderPayment.Status.PAID);
            repo.save(op);

            Map<String, Object> resp = new HashMap<>();
            resp.put("provider", "mock");
            resp.put("status", "PAID");
            resp.put("orderId", op.getOrderId());
            resp.put("paymentId", op.getPaymentId());
            resp.put("amount", amountPaise.intValue());
            resp.put("currency", "INR");
            return resp;
        }

        // RAZORPAY path
        try {
            String credentials = razorKeyId + ":" + razorKeySecret;
            String basicAuth = "Basic " + Base64Utils.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("amount", amountPaise.intValue());
            reqBody.put("currency", "INR");
            reqBody.put("receipt", "rcpt_" + System.currentTimeMillis());
            reqBody.put("payment_capture", 1);

            var httpClient = java.net.http.HttpClient.newHttpClient();
            var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            var httpReq = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.razorpay.com/v1/orders"))
                    .header("Authorization", basicAuth)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(reqBody)))
                    .build();

            var httpResp = httpClient.send(httpReq, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (httpResp.statusCode() >= 200 && httpResp.statusCode() < 300) {
                Map<?, ?> map = objectMapper.readValue(httpResp.body(), java.util.Map.class);
                String orderId = (String) map.get("id");

                if (orderId == null || orderId.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Razorpay order id missing in response");
                }

                op.setOrderId(orderId);
                // (Optional improvement) if your enum has it:
                // op.setStatus(OrderPayment.Status.PENDING);
                repo.save(op);

                Map<String, Object> resp = new HashMap<>();
                resp.put("provider", "razorpay");
                resp.put("orderId", orderId);
                resp.put("amount", amountPaise.intValue());
                resp.put("currency", "INR");
                resp.put("keyId", razorKeyId);
                return resp;
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Razorpay create order failed. status=" + httpResp.statusCode() + " body=" + httpResp.body()
            );

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public Map<String, Object> verify(Map<String, String> body, String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing fields");
        }

        String orderId = body.get("razorpay_order_id");
        String paymentId = body.get("razorpay_payment_id");
        String signature = body.get("razorpay_signature");

        if (orderId == null || paymentId == null || signature == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing fields");
        }

        OrderPayment op = repo.findByOrderId(orderId);
        if (op == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order not found");
        }
        if (!username.equals(op.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        if (!"razorpay".equalsIgnoreCase(provider)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provider is not razorpay");
        }
        if (isBlank(razorKeySecret)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Razorpay secret not configured");
        }

        try {
            String data = orderId + "|" + paymentId;

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorKeySecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            sha256_HMAC.init(secretKey);

            String expected = bytesToHex(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));

            if (expected.equalsIgnoreCase(signature)) {
                op.setPaymentId(paymentId);
                op.setStatus(OrderPayment.Status.PAID);
                repo.save(op);

                // ✅ STEP 6: fulfill AFTER payment is PAID (idempotent)
                checkoutFulfillmentService.fulfill(orderId, username);

                return Map.of("status", "PAID");
            } else {
                op.setStatus(OrderPayment.Status.FAILED);
                repo.save(op);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Signature mismatch");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    public void attachCheckoutItems(String orderId, String checkoutItemsJson, String username) {
        OrderPayment op = repo.findByOrderId(orderId);
        if (op == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order not found");
        if (!username.equals(op.getUsername())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");

        op.setCheckoutItemsJson(checkoutItemsJson);
        repo.save(op);
    }


    public List<OrderPayment> allOrders() {
        return repo.findAll();
    }

    public List<OrderPayment> myOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return repo.findByUsernameOrderByCreatedAtDesc(username);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
