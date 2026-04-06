package org.tavall.platform.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.persistence.entity.RuntimeMutationHistory;

public interface RuntimeMutationHistoryRepository extends JpaRepository<RuntimeMutationHistory, UUID> {

    List<RuntimeMutationHistory> findByTenantSiteIdOrderByCreatedAtDesc(UUID tenantSiteId);
}
