package org.tavall.platform.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.persistence.entity.TenantAccount;

public interface TenantAccountRepository extends JpaRepository<TenantAccount, UUID> {

    Optional<TenantAccount> findBySlug(String slug);
}
