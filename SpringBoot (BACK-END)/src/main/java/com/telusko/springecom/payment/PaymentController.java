package com.telusko.springecom.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${payments.provider:mock}")
    private String provider; // razorpay | mock

    @Value("${razorpay.keyId:}")
    private String razorKeyId;

    @Value("${razorpay.keySecret:}")
    private String razorKeySecret;

    private final OrderPaymentRepository repo;

    public PaymentController(OrderPaymentRepository repo){
        this.repo = repo;
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body, Authentication auth) {
        BigDecimal amountRs = new BigDecimal(String.valueOf(body.getOrDefault("amount", "1")));
        BigDecimal amountPaise = amountRs.multiply(new BigDecimal("100"));

        OrderPayment op = new OrderPayment();
        op.setUsername(auth.getName());
        op.setAmount(amountRs);
        op.setProvider(provider);

        if(!"razorpay".equalsIgnoreCase(provider) || razorKeyId.isEmpty() || razorKeySecret.isEmpty()){
            op.setOrderId("order_mock_" + System.currentTimeMillis());
            op.setPaymentId("pay_mock_" + System.nanoTime());
            op.setStatus(OrderPayment.Status.PAID);
            repo.save(op);
            Map<String,Object> resp = new HashMap<>();
            resp.put("provider","mock");
            resp.put("status","PAID");
            resp.put("orderId", op.getOrderId());
            resp.put("paymentId", op.getPaymentId());
            resp.put("amount", amountPaise.intValue());
            resp.put("currency","INR");
            return ResponseEntity.ok(resp);
        }

        try {
            String credentials = razorKeyId + ":" + razorKeySecret;
            String basicAuth = "Basic " + Base64Utils.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            var reqBody = new HashMap<String,Object>();
            reqBody.put("amount", amountPaise.intValue());
            reqBody.put("currency", "INR");
            reqBody.put("receipt", "rcpt_"+System.currentTimeMillis());
            reqBody.put("payment_capture", 1);

            var httpClient = java.net.http.HttpClient.newHttpClient();
            var httpReq = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.razorpay.com/v1/orders"))
                    .header("Authorization", basicAuth)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(reqBody)))
                    .build();
            var httpResp = httpClient.send(httpReq, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (httpResp.statusCode() >= 200 && httpResp.statusCode() < 300) {
                var map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(httpResp.body(), java.util.Map.class);
                String orderId = (String) map.get("id");
                op.setOrderId(orderId);
                repo.save(op);

                Map<String,Object> resp = new HashMap<>();
                resp.put("provider","razorpay");
                resp.put("orderId", orderId);
                resp.put("amount", amountPaise.intValue());
                resp.put("currency","INR");
                resp.put("keyId", razorKeyId);
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(502).body(Map.of("error","Razorpay create order failed", "status", httpResp.statusCode(), "body", httpResp.body()));
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body, Authentication auth){
        String orderId = body.get("razorpay_order_id");
        String paymentId = body.get("razorpay_payment_id");
        String signature = body.get("razorpay_signature");

        if(orderId == null || paymentId == null || signature == null){
            return ResponseEntity.badRequest().body(Map.of("error","Missing fields"));
        }

        OrderPayment op = repo.findByOrderId(orderId);
        if(op == null) return ResponseEntity.badRequest().body(Map.of("error","Order not found"));
        if(!op.getUsername().equals(auth.getName())) return ResponseEntity.status(403).body(Map.of("error","Forbidden"));

        try {
            String data = orderId + "|" + paymentId;
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(razorKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String expected = bytesToHex(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));

            if(expected.equalsIgnoreCase(signature)){
                op.setPaymentId(paymentId);
                op.setStatus(OrderPayment.Status.PAID);
                repo.save(op);
                return ResponseEntity.ok(Map.of("status","PAID"));
            }else{
                op.setStatus(OrderPayment.Status.FAILED);
                repo.save(op);
                return ResponseEntity.status(400).body(Map.of("status","FAILED","error","Signature mismatch"));
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<OrderPayment> allOrders(){
        return repo.findAll();
    }

@GetMapping("/my")
    public List<OrderPayment> myOrders(Authentication auth){
        return repo.findByUsernameOrderByCreatedAtDesc(auth.getName());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b: bytes){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
