package org.tavall.webstore.cart.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.cart.model.CartStatus;
import org.tavall.webstore.cart.model.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @EntityGraph(attributePaths = {"lineItems", "lineItems.product", "lineItems.variant"})
    Optional<ShoppingCart> findDetailedByTokenAndStatus(String token, CartStatus status);
}
