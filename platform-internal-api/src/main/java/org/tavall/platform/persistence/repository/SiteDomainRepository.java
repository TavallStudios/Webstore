package org.tavall.platform.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.persistence.entity.SiteDomain;

public interface SiteDomainRepository extends JpaRepository<SiteDomain, UUID> {

    List<SiteDomain> findByTenantSiteIdOrderByPrimaryDomainDescCreatedAtAsc(UUID tenantSiteId);

    Optional<SiteDomain> findByHostIgnoreCase(String host);
}
