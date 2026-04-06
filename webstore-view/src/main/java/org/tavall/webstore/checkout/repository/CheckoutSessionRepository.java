package org.tavall.webstore.checkout.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.checkout.model.CheckoutSession;

public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {

    @EntityGraph(attributePaths = {"cart", "cart.lineItems", "cart.lineItems.product", "cart.lineItems.variant"})
    Optional<CheckoutSession> findDetailedByExternalReference(String externalReference);
}
