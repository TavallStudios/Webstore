package org.tavall.webstore.catalog.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.catalog.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findAllByActiveTrueOrderByAutomaticDescDisplayNameAsc();

    Optional<Promotion> findByOfferCodeIgnoreCaseAndActiveTrue(String offerCode);
}
