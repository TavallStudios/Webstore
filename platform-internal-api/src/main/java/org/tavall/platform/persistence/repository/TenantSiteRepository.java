package org.tavall.platform.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.core.TenantSiteLifecycleState;
import org.tavall.platform.persistence.entity.TenantSite;

public interface TenantSiteRepository extends JpaRepository<TenantSite, UUID> {

    List<TenantSite> findByTenantAccountIdOrderByCreatedAtDesc(UUID tenantAccountId);

    List<TenantSite> findTop20ByOrderByUpdatedAtDesc();

    List<TenantSite> findByLifecycleStateIn(List<TenantSiteLifecycleState> lifecycleStates);

    long countByLifecycleState(TenantSiteLifecycleState lifecycleState);
}
