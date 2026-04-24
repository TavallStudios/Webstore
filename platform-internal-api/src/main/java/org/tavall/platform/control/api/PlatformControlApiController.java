package org.tavall.platform.control.api;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tavall.platform.control.service.TenantSiteRuntimeControlService;
import org.tavall.platform.core.command.AssignTenantSiteDomainCommand;
import org.tavall.platform.core.command.MutateTenantSiteInfrastructureProfileCommand;
import org.tavall.platform.core.command.MutateTenantSiteRuntimeResourcesCommand;
import org.tavall.platform.core.command.PublishTenantSiteVersionCommand;
import org.tavall.platform.persistence.entity.SiteDomain;
import org.tavall.platform.persistence.entity.SitePublication;
import org.tavall.platform.persistence.entity.TenantSite;
import org.tavall.platform.runtime.KubeVirtClusterCompatibilityReport;
import org.tavall.platform.runtime.TenantSiteDeploymentResult;
import org.tavall.platform.runtime.TenantSiteRuntimeStatus;

@RestController
@RequestMapping("/internal/control/sites")
public class PlatformControlApiController {

    private final TenantSiteRuntimeControlService tenantSiteRuntimeControlService;

    public PlatformControlApiController(TenantSiteRuntimeControlService tenantSiteRuntimeControlService) {
        this.tenantSiteRuntimeControlService = tenantSiteRuntimeControlService;
    }

    @PostMapping("/{siteId}/launch")
    public TenantSiteDeploymentResult launchSite(@PathVariable UUID siteId, @Valid @RequestBody RequestedByPayload payload) {
        return tenantSiteRuntimeControlService.launchTenantSiteRuntime(siteId, payload.requestedByUserId());
    }

    @PostMapping("/{siteId}/stop")
    public TenantSiteRuntimeStatus stopSite(@PathVariable UUID siteId, @Valid @RequestBody RequestedByPayload payload) {
        return tenantSiteRuntimeControlService.stopTenantSiteRuntime(siteId, payload.requestedByUserId());
    }

    @PostMapping("/{siteId}/restart")
    public TenantSiteRuntimeStatus restartSite(@PathVariable UUID siteId, @Valid @RequestBody RequestedByPayload payload) {
        return tenantSiteRuntimeControlService.restartTenantSiteRuntime(siteId, payload.requestedByUserId());
    }

    @DeleteMapping("/{siteId}")
    public TenantSiteRuntimeStatus destroySite(@PathVariable UUID siteId, @Valid @RequestBody RequestedByPayload payload) {
        return tenantSiteRuntimeControlService.destroyTenantSiteRuntime(siteId, payload.requestedByUserId());
    }

    @PutMapping("/{siteId}/resources")
    public TenantSiteRuntimeStatus mutateResources(
            @PathVariable UUID siteId,
            @Valid @RequestBody MutateTenantSiteRuntimeResourcesCommand command
    ) {
        if (!siteId.equals(command.siteId())) {
            throw new IllegalArgumentException("Payload siteId must match the path siteId.");
        }
        return tenantSiteRuntimeControlService.mutateTenantSiteRuntimeResources(command);
    }

    @PutMapping("/{siteId}/infrastructure-profile")
    public TenantSiteRuntimeStatus mutateInfrastructureProfile(
            @PathVariable UUID siteId,
            @Valid @RequestBody MutateTenantSiteInfrastructureProfileCommand command
    ) {
        if (!siteId.equals(command.siteId())) {
            throw new IllegalArgumentException("Payload siteId must match the path siteId.");
        }
        return tenantSiteRuntimeControlService.mutateTenantSiteInfrastructureProfile(command);
    }

    @PutMapping("/{siteId}/domain")
    public SiteDomain assignDomain(@PathVariable UUID siteId, @Valid @RequestBody AssignTenantSiteDomainCommand command) {
        if (!siteId.equals(command.siteId())) {
            throw new IllegalArgumentException("Payload siteId must match the path siteId.");
        }
        return tenantSiteRuntimeControlService.assignTenantSiteDomain(command);
    }

    @PostMapping("/{siteId}/publications")
    public SitePublication publishVersion(@PathVariable UUID siteId, @Valid @RequestBody PublishTenantSiteVersionCommand command) {
        if (!siteId.equals(command.siteId())) {
            throw new IllegalArgumentException("Payload siteId must match the path siteId.");
        }
        return tenantSiteRuntimeControlService.publishTenantSiteVersion(command);
    }

    @PostMapping("/{siteId}/mark-ready")
    public TenantSite markSiteReady(@PathVariable UUID siteId, @Valid @RequestBody RequestedByPayload payload) {
        return tenantSiteRuntimeControlService.markTenantSiteReady(siteId, payload.requestedByUserId());
    }

    @PostMapping("/{siteId}/sync")
    public TenantSiteRuntimeStatus synchronizeStatus(@PathVariable UUID siteId, @Valid @RequestBody RequestedByPayload payload) {
        return tenantSiteRuntimeControlService.synchronizeTenantSiteRuntimeStatus(siteId, payload.requestedByUserId());
    }

    @GetMapping("/{siteId}/status")
    public TenantSiteRuntimeStatus loadStatus(@PathVariable UUID siteId) {
        return tenantSiteRuntimeControlService.loadTenantSiteRuntimeStatus(siteId);
    }

    @GetMapping("/{siteId}/compatibility")
    public KubeVirtClusterCompatibilityReport loadCompatibility(@PathVariable UUID siteId) {
        return tenantSiteRuntimeControlService.loadClusterCompatibility(siteId);
    }
}
