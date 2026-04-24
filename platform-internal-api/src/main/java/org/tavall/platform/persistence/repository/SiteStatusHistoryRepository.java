package org.tavall.platform.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.persistence.entity.SiteStatusHistory;

public interface SiteStatusHistoryRepository extends JpaRepository<SiteStatusHistory, UUID> {

    List<SiteStatusHistory> findTop25ByTenantSiteIdOrderByCreatedAtDesc(UUID tenantSiteId);
}
