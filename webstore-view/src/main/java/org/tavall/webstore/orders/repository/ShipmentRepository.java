package org.tavall.webstore.orders.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.orders.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @EntityGraph(attributePaths = {"trackingEvents"})
    Optional<Shipment> findByCustomerOrderOrderNumber(String orderNumber);
}
