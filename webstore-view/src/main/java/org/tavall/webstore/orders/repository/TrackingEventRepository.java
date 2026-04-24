package org.tavall.webstore.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.orders.model.TrackingEvent;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
}
