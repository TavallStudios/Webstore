package org.tavall.platform.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.core.SitePublicationStatus;
import org.tavall.platform.persistence.entity.SitePublication;

public interface SitePublicationRepository extends JpaRepository<SitePublication, UUID> {

    List<SitePublication> findByTenantSiteIdOrderByCreatedAtDesc(UUID tenantSiteId);

    Optional<SitePublication> findFirstByTenantSiteIdAndPublicationStatusOrderByCreatedAtDesc(
            UUID tenantSiteId,
            SitePublicationStatus publicationStatus
    );
}
