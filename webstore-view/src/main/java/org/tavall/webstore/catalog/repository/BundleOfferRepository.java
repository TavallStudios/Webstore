package org.tavall.webstore.catalog.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.catalog.model.BundleOffer;

public interface BundleOfferRepository extends JpaRepository<BundleOffer, Long> {

    List<BundleOffer> findAllByProductIdOrderByIdAsc(Long productId);
}
