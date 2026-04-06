package org.tavall.platform.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.core.PlatformProviderType;
import org.tavall.platform.persistence.entity.PlatformProviderIdentity;

public interface PlatformProviderIdentityRepository extends JpaRepository<PlatformProviderIdentity, UUID> {

    Optional<PlatformProviderIdentity> findByProviderTypeAndProviderSubject(PlatformProviderType providerType, String providerSubject);
}
