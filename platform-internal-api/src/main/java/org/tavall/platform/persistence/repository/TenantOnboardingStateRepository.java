package org.tavall.platform.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.persistence.entity.TenantOnboardingState;

public interface TenantOnboardingStateRepository extends JpaRepository<TenantOnboardingState, UUID> {

    Optional<TenantOnboardingState> findByTenantAccountId(UUID tenantAccountId);
}
