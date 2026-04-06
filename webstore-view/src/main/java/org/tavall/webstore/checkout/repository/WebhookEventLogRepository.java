package org.tavall.webstore.checkout.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.checkout.model.CheckoutProvider;
import org.tavall.webstore.checkout.model.WebhookEventLog;

public interface WebhookEventLogRepository extends JpaRepository<WebhookEventLog, Long> {

    boolean existsByProviderAndEventId(CheckoutProvider provider, String eventId);
}
