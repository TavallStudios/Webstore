package org.tavall.platform.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.platform.core.OrchestrationJobStatus;
import org.tavall.platform.persistence.entity.OrchestrationJob;

public interface OrchestrationJobRepository extends JpaRepository<OrchestrationJob, UUID> {

    List<OrchestrationJob> findTop50ByOrderByQueuedAtDesc();

    List<OrchestrationJob> findByJobStatusInOrderByQueuedAtAsc(List<OrchestrationJobStatus> jobStatuses);
}
