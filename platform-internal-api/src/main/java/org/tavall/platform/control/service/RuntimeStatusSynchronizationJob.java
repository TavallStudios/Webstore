package org.tavall.platform.control.service;

import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tavall.platform.core.TenantSiteLifecycleState;
import org.tavall.platform.persistence.repository.TenantSiteRepository;

@Component
public class RuntimeStatusSynchronizationJob {

    private final TenantSiteRepository tenantSiteRepository;
    private final TenantSiteRuntimeControlService tenantSiteRuntimeControlService;

    public RuntimeStatusSynchronizationJob(
            TenantSiteRepository tenantSiteRepository,
            TenantSiteRuntimeControlService tenantSiteRuntimeControlService
    ) {
        this.tenantSiteRepository = tenantSiteRepository;
        this.tenantSiteRuntimeControlService = tenantSiteRuntimeControlService;
    }

    @Scheduled(fixedDelayString = "${platform.control.sync.fixed-delay-ms:30000}")
    public void synchronizeActiveRuntimes() {
        tenantSiteRepository.findByLifecycleStateIn(List.of(
                TenantSiteLifecycleState.PROVISIONING,
                TenantSiteLifecycleState.RUNNING,
                TenantSiteLifecycleState.UPDATING,
                TenantSiteLifecycleState.STOPPED
        )).forEach(site -> tenantSiteRuntimeControlService.synchronizeTenantSiteRuntimeStatus(site.getId(), site.getCreatedByUserId()));
    }
}
