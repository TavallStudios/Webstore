package org.tavall.webstore.orders.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.orders.model.PaymentRecord;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    List<PaymentRecord> findAllByCheckoutSessionIdOrderByCreatedAtAsc(Long checkoutSessionId);
}
