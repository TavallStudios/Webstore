package org.tavall.platform.admin.controller;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.tavall.platform.admin.model.DomainAssignmentForm;
import org.tavall.platform.admin.model.PublicationForm;
import org.tavall.platform.admin.model.RuntimeInfrastructureForm;
import org.tavall.platform.admin.model.RuntimeResourceForm;
import org.tavall.platform.admin.service.PlatformAdminAccessService;
import org.tavall.platform.admin.service.PlatformAdminQueryService;
import org.tavall.platform.auth.ConfiguredOAuthProviderService;
import org.tavall.platform.control.service.TenantSiteRuntimeControlService;
import org.tavall.platform.core.command.AssignTenantSiteDomainCommand;
import org.tavall.platform.core.command.MutateTenantSiteInfrastructureProfileCommand;
import org.tavall.platform.core.command.MutateTenantSiteRuntimeResourcesCommand;
import org.tavall.platform.core.command.PublishTenantSiteVersionCommand;
import org.tavall.platform.core.exception.PlatformDomainException;
import org.tavall.platform.runtime.KubeVirtClusterCompatibilityReport;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;
import org.tavall.platform.runtime.TenantSiteRuntimeResources;
import org.tavall.platform.runtime.TenantSiteRuntimeStatus;

@Controller
public class PlatformAdminController {

    private final PlatformAdminAccessService platformAdminAccessService;
    private final PlatformAdminQueryService platformAdminQueryService;
    private final ConfiguredOAuthProviderService configuredOAuthProviderService;
    private final TenantSiteRuntimeControlService tenantSiteRuntimeControlService;

    public PlatformAdminController(
            PlatformAdminAccessService platformAdminAccessService,
            PlatformAdminQueryService platformAdminQueryService,
            ConfiguredOAuthProviderService configuredOAuthProviderService,
            TenantSiteRuntimeControlService tenantSiteRuntimeControlService
    ) {
        this.platformAdminAccessService = platformAdminAccessService;
        this.platformAdminQueryService = platformAdminQueryService;
        this.configuredOAuthProviderService = configuredOAuthProviderService;
        this.tenantSiteRuntimeControlService = tenantSiteRuntimeControlService;
    }

    @GetMapping("/admin/login")
    public String login(Model model) {
        model.addAttribute("providers", configuredOAuthProviderService.listConfiguredProviders());
        return "admin/login";
    }

    @GetMapping("/admin")
    public String dashboard(Model model, HttpSession session) {
        platformAdminAccessService.requireMasterAdmin(session);
        model.addAttribute("summary", platformAdminQueryService.loadDashboardSummary());
        return "admin/dashboard";
    }

    @GetMapping("/admin/tenants")
    public String tenants(Model model, HttpSession session) {
        platformAdminAccessService.requireMasterAdmin(session);
        model.addAttribute("tenants", platformAdminQueryService.loadTenantRows());
        return "admin/tenants";
    }

    @GetMapping("/admin/sites")
    public String sites(Model model, HttpSession session) {
        platformAdminAccessService.requireMasterAdmin(session);
        model.addAttribute("sites", platformAdminQueryService.loadSiteRows());
        return "admin/sites";
    }

    @GetMapping("/admin/sites/{siteId}")
    public String siteDetail(@PathVariable UUID siteId, Model model, HttpSession session) {
        platformAdminAccessService.requireMasterAdmin(session);
        model.addAttribute("detail", platformAdminQueryService.loadSiteDetail(siteId, loadRuntimeStatus(siteId)));
        model.addAttribute("compatibilityReport", loadCompatibility(siteId));
        model.addAttribute("infrastructureProfiles", TenantRuntimeInfrastructureProfile.values());
        RuntimeResourceForm resourceForm = new RuntimeResourceForm();
        resourceForm.setSiteId(siteId);
        RuntimeInfrastructureForm infrastructureForm = new RuntimeInfrastructureForm();
        infrastructureForm.setSiteId(siteId);
        infrastructureForm.setInfrastructureProfile(loadInfrastructureProfile(siteId));
        DomainAssignmentForm domainForm = new DomainAssignmentForm();
        domainForm.setSiteId(siteId);
        PublicationForm publicationForm = new PublicationForm();
        publicationForm.setSiteId(siteId);
        publicationForm.setVersionLabel("v-next");
        publicationForm.setBuildReference("webstore-view:latest");
        model.addAttribute("resourceForm", resourceForm);
        model.addAttribute("infrastructureForm", infrastructureForm);
        model.addAttribute("domainForm", domainForm);
        model.addAttribute("publicationForm", publicationForm);
        return "admin/site-detail";
    }

    @GetMapping("/admin/deployments")
    public String deployments(Model model, HttpSession session) {
        platformAdminAccessService.requireMasterAdmin(session);
        model.addAttribute("deployments", platformAdminQueryService.loadDeploymentRows());
        return "admin/deployments";
    }

    @GetMapping("/admin/operations")
    public String operations(Model model, HttpSession session) {
        platformAdminAccessService.requireMasterAdmin(session);
        model.addAttribute("jobs", platformAdminQueryService.loadJobRows());
        return "admin/operations";
    }

    @GetMapping("/admin/audit")
    public String audit(Model model, HttpSession session) {
        platformAdminAccessService.requireMasterAdmin(session);
        model.addAttribute("audits", platformAdminQueryService.loadAuditRows());
        return "admin/audit";
    }

    @PostMapping("/admin/sites/{siteId}/launch")
    public String launch(@PathVariable UUID siteId, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.launchTenantSiteRuntime(siteId, admin.platformUserId());
        return "redirect:/admin/sites/" + siteId;
    }

    @PostMapping("/admin/sites/{siteId}/stop")
    public String stop(@PathVariable UUID siteId, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.stopTenantSiteRuntime(siteId, admin.platformUserId());
        return "redirect:/admin/sites/" + siteId;
    }

    @PostMapping("/admin/sites/{siteId}/restart")
    public String restart(@PathVariable UUID siteId, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.restartTenantSiteRuntime(siteId, admin.platformUserId());
        return "redirect:/admin/sites/" + siteId;
    }

    @PostMapping("/admin/sites/{siteId}/destroy")
    public String destroy(@PathVariable UUID siteId, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.destroyTenantSiteRuntime(siteId, admin.platformUserId());
        return "redirect:/admin/sites";
    }

    @PostMapping("/admin/sites/{siteId}/sync")
    public String sync(@PathVariable UUID siteId, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.synchronizeTenantSiteRuntimeStatus(siteId, admin.platformUserId());
        return "redirect:/admin/sites/" + siteId;
    }

    @PostMapping("/admin/sites/{siteId}/mark-ready")
    public String markReady(@PathVariable UUID siteId, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.markTenantSiteReady(siteId, admin.platformUserId());
        return "redirect:/admin/sites/" + siteId;
    }

    @PostMapping("/admin/sites/{siteId}/resources")
    public String mutateResources(@PathVariable UUID siteId, @ModelAttribute RuntimeResourceForm form, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.mutateTenantSiteRuntimeResources(new MutateTenantSiteRuntimeResourcesCommand(
                siteId,
                admin.platformUserId(),
                new TenantSiteRuntimeResources(form.getCpuCores(), form.getMemoryMiB(), form.getStorageGiB())
        ));
        return "redirect:/admin/sites/" + siteId;
    }

    @PostMapping("/admin/sites/{siteId}/infrastructure-profile")
    public String mutateInfrastructureProfile(@PathVariable UUID siteId, @ModelAttribute RuntimeInfrastructureForm form, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.mutateTenantSiteInfrastructureProfile(new MutateTenantSiteInfrastructureProfileCommand(
                siteId,
                admin.platformUserId(),
                form.getInfrastructureProfile()
        ));
        return "redirect:/admin/sites/" + siteId;
    }

    @PostMapping("/admin/sites/{siteId}/domain")
    public String assignDomain(@PathVariable UUID siteId, @ModelAttribute DomainAssignmentForm form, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.assignTenantSiteDomain(new AssignTenantSiteDomainCommand(siteId, admin.platformUserId(), form.getHost(), form.isPrimaryDomain()));
        return "redirect:/admin/sites/" + siteId;
    }

    @PostMapping("/admin/sites/{siteId}/publish")
    public String publish(@PathVariable UUID siteId, @ModelAttribute PublicationForm form, HttpSession session) {
        var admin = platformAdminAccessService.requireMasterAdmin(session);
        tenantSiteRuntimeControlService.publishTenantSiteVersion(new PublishTenantSiteVersionCommand(
                siteId,
                admin.platformUserId(),
                form.getVersionLabel(),
                form.getBuildReference(),
                Map.of(),
                Map.of(),
                Map.of("publishedFrom", "platform-spring-webview-admin")
        ));
        return "redirect:/admin/sites/" + siteId;
    }

    private TenantSiteRuntimeStatus loadRuntimeStatus(UUID siteId) {
        try {
            return tenantSiteRuntimeControlService.loadTenantSiteRuntimeStatus(siteId);
        } catch (PlatformDomainException exception) {
            return null;
        }
    }

    private KubeVirtClusterCompatibilityReport loadCompatibility(UUID siteId) {
        try {
            return tenantSiteRuntimeControlService.loadClusterCompatibility(siteId);
        } catch (PlatformDomainException exception) {
            return null;
        }
    }

    private TenantRuntimeInfrastructureProfile loadInfrastructureProfile(UUID siteId) {
        try {
            return tenantSiteRuntimeControlService.loadInfrastructureProfile(siteId);
        } catch (PlatformDomainException exception) {
            return TenantRuntimeInfrastructureProfile.AUTO;
        }
    }
}
