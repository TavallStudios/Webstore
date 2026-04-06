package org.tavall.platform.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.persistence.entity.SiteDeploymentRecord;

public interface SiteDeploymentRecordRepository extends JpaRepository<SiteDeploymentRecord, UUID> {

    List<SiteDeploymentRecord> findByTenantSiteIdOrderByCreatedAtDesc(UUID tenantSiteId);

    List<SiteDeploymentRecord> findTop25ByOrderByCreatedAtDesc();
}
