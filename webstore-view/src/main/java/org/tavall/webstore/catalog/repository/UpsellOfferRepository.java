package org.tavall.webstore.catalog.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.catalog.model.UpsellOffer;

public interface UpsellOfferRepository extends JpaRepository<UpsellOffer, Long> {

    List<UpsellOffer> findAllByProductIdOrderByIdAsc(Long productId);
}
