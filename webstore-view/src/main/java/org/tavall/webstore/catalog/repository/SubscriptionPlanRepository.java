package org.tavall.webstore.catalog.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.catalog.model.SubscriptionPlan;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    List<SubscriptionPlan> findAllByProductIdOrderByIdAsc(Long productId);
}
