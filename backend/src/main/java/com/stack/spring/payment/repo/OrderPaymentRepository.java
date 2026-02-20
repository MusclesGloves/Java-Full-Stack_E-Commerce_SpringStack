package com.stack.spring.payment.repo;

import com.stack.spring.payment.model.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
    List<OrderPayment> findByUsernameOrderByCreatedAtDesc(String username);
    OrderPayment findByOrderId(String orderId);
}
