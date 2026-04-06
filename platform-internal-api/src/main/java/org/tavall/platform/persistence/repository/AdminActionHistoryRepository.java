package org.tavall.platform.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.persistence.entity.AdminActionHistory;

public interface AdminActionHistoryRepository extends JpaRepository<AdminActionHistory, UUID> {

    List<AdminActionHistory> findTop50ByOrderByCreatedAtDesc();
}
