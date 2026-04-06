package org.tavall.webstore.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.cart.model.CartLineItem;

public interface CartLineItemRepository extends JpaRepository<CartLineItem, Long> {
}
