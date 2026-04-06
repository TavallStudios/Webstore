package org.tavall.webstore.catalog.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.catalog.model.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findAllByProductIdOrderBySortOrderAscIdAsc(Long productId);
}
