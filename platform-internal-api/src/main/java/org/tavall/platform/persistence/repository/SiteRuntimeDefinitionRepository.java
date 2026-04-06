package org.tavall.platform.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.persistence.entity.SiteRuntimeDefinition;

public interface SiteRuntimeDefinitionRepository extends JpaRepository<SiteRuntimeDefinition, UUID> {

    Optional<SiteRuntimeDefinition> findByTenantSiteId(UUID tenantSiteId);
}
