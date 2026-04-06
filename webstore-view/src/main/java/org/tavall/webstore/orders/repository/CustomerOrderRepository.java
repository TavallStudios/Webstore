package org.tavall.webstore.orders.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.orders.model.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    Optional<CustomerOrder> findByCheckoutSessionId(Long checkoutSessionId);

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
}
