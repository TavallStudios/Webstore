package org.tavall.platform.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.persistence.entity.TenantMembership;

public interface TenantMembershipRepository extends JpaRepository<TenantMembership, UUID> {

    List<TenantMembership> findByPlatformUserIdOrderByCreatedAtAsc(UUID platformUserId);

    List<TenantMembership> findByTenantAccountIdOrderByCreatedAtAsc(UUID tenantAccountId);

    Optional<TenantMembership> findByPlatformUserIdAndTenantAccountId(UUID platformUserId, UUID tenantAccountId);
}
