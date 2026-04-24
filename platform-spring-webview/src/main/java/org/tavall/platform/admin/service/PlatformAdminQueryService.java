package org.tavall.platform.admin.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.platform.admin.view.PlatformAdminViewModels;
import org.tavall.platform.core.SitePublicationStatus;
import org.tavall.platform.core.TenantSiteLifecycleState;
import org.tavall.platform.persistence.entity.RuntimeMutationHistory;
import org.tavall.platform.persistence.entity.SiteDeploymentRecord;
import org.tavall.platform.persistence.entity.SiteDomain;
import org.tavall.platform.persistence.entity.SitePublication;
import org.tavall.platform.persistence.entity.SiteRuntimeDefinition;
import org.tavall.platform.persistence.entity.SiteStatusHistory;
import org.tavall.platform.persistence.entity.TenantMembership;
import org.tavall.platform.persistence.entity.TenantOnboardingState;
import org.tavall.platform.persistence.entity.TenantSite;
import org.tavall.platform.persistence.repository.AdminActionHistoryRepository;
import org.tavall.platform.persistence.repository.OrchestrationJobRepository;
import org.tavall.platform.persistence.repository.PlatformUserRepository;
import org.tavall.platform.persistence.repository.RuntimeMutationHistoryRepository;
import org.tavall.platform.persistence.repository.SiteDeploymentRecordRepository;
import org.tavall.platform.persistence.repository.SiteDomainRepository;
import org.tavall.platform.persistence.repository.SitePublicationRepository;
import org.tavall.platform.persistence.repository.SiteRuntimeDefinitionRepository;
import org.tavall.platform.persistence.repository.SiteStatusHistoryRepository;
import org.tavall.platform.persistence.repository.TenantMembershipRepository;
import org.tavall.platform.persistence.repository.TenantOnboardingStateRepository;
import org.tavall.platform.persistence.repository.TenantSiteRepository;
import org.tavall.platform.runtime.TenantSiteRuntimeStatus;

@Service
public class PlatformAdminQueryService {

    private final PlatformUserRepository platformUserRepository;
    private final TenantMembershipRepository tenantMembershipRepository;
    private final TenantOnboardingStateRepository tenantOnboardingStateRepository;
    private final TenantSiteRepository tenantSiteRepository;
    private final SiteRuntimeDefinitionRepository siteRuntimeDefinitionRepository;
    private final SiteDomainRepository siteDomainRepository;
    private final SitePublicationRepository sitePublicationRepository;
    private final SiteDeploymentRecordRepository siteDeploymentRecordRepository;
    private final SiteStatusHistoryRepository siteStatusHistoryRepository;
    private final RuntimeMutationHistoryRepository runtimeMutationHistoryRepository;
    private final OrchestrationJobRepository orchestrationJobRepository;
    private final AdminActionHistoryRepository adminActionHistoryRepository;

    public PlatformAdminQueryService(
            PlatformUserRepository platformUserRepository,
            TenantMembershipRepository tenantMembershipRepository,
            TenantOnboardingStateRepository tenantOnboardingStateRepository,
            TenantSiteRepository tenantSiteRepository,
            SiteRuntimeDefinitionRepository siteRuntimeDefinitionRepository,
            SiteDomainRepository siteDomainRepository,
            SitePublicationRepository sitePublicationRepository,
            SiteDeploymentRecordRepository siteDeploymentRecordRepository,
            SiteStatusHistoryRepository siteStatusHistoryRepository,
            RuntimeMutationHistoryRepository runtimeMutationHistoryRepository,
            OrchestrationJobRepository orchestrationJobRepository,
            AdminActionHistoryRepository adminActionHistoryRepository
    ) {
        this.platformUserRepository = platformUserRepository;
        this.tenantMembershipRepository = tenantMembershipRepository;
        this.tenantOnboardingStateRepository = tenantOnboardingStateRepository;
        this.tenantSiteRepository = tenantSiteRepository;
        this.siteRuntimeDefinitionRepository = siteRuntimeDefinitionRepository;
        this.siteDomainRepository = siteDomainRepository;
        this.sitePublicationRepository = sitePublicationRepository;
        this.siteDeploymentRecordRepository = siteDeploymentRecordRepository;
        this.siteStatusHistoryRepository = siteStatusHistoryRepository;
        this.runtimeMutationHistoryRepository = runtimeMutationHistoryRepository;
        this.orchestrationJobRepository = orchestrationJobRepository;
        this.adminActionHistoryRepository = adminActionHistoryRepository;
    }

    @Transactional(readOnly = true)
    public PlatformAdminViewModels.AdminDashboardSummary loadDashboardSummary() {
        return new PlatformAdminViewModels.AdminDashboardSummary(
                tenantMembershipRepository.findAll().stream().map(TenantMembership::getTenantAccount).map(account -> account.getId()).distinct().count(),
                tenantSiteRepository.count(),
                tenantSiteRepository.countByLifecycleState(TenantSiteLifecycleState.RUNNING),
                tenantSiteRepository.countByLifecycleState(TenantSiteLifecycleState.STOPPED),
                tenantSiteRepository.countByLifecycleState(TenantSiteLifecycleState.FAILED),
                siteDeploymentRecordRepository.findTop25ByOrderByCreatedAtDesc().stream().map(this::toDeploymentRow).toList(),
                orchestrationJobRepository.findTop50ByOrderByQueuedAtDesc().stream().limit(10).map(job ->
                        new PlatformAdminViewModels.AdminJobRow(
                                job.getTenantSite().getSiteName(),
                                job.getJobType().name(),
                                job.getJobStatus().name(),
                                job.getRetryCount(),
                                job.getLastErrorMessage(),
                                job.getQueuedAt()
                        )).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<PlatformAdminViewModels.AdminTenantRow> loadTenantRows() {
        return platformUserRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .flatMap(user -> tenantMembershipRepository.findByPlatformUserIdOrderByCreatedAtAsc(user.getId()).stream()
                        .map(membership -> toTenantRow(user.getId(), membership, tenantOnboardingStateRepository.findByTenantAccountId(membership.getTenantAccount().getId()).orElse(null))))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlatformAdminViewModels.AdminSiteRow> loadSiteRows() {
        return tenantSiteRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
                .map(this::toSiteRow)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatformAdminViewModels.AdminSiteDetailView loadSiteDetail(UUID siteId, TenantSiteRuntimeStatus runtimeStatus) {
        TenantSite site = tenantSiteRepository.findById(siteId).orElseThrow();
        SiteRuntimeDefinition runtimeDefinition = siteRuntimeDefinitionRepository.findByTenantSiteId(siteId).orElse(null);
        List<SiteDomain> domains = siteDomainRepository.findByTenantSiteIdOrderByPrimaryDomainDescCreatedAtAsc(siteId);
        List<SitePublication> publications = sitePublicationRepository.findByTenantSiteIdOrderByCreatedAtDesc(siteId);
        List<SiteDeploymentRecord> deployments = siteDeploymentRecordRepository.findByTenantSiteIdOrderByCreatedAtDesc(siteId);
        List<SiteStatusHistory> statusHistory = siteStatusHistoryRepository.findTop25ByTenantSiteIdOrderByCreatedAtDesc(siteId);
        List<RuntimeMutationHistory> mutations = runtimeMutationHistoryRepository.findByTenantSiteIdOrderByCreatedAtDesc(siteId);
        return new PlatformAdminViewModels.AdminSiteDetailView(
                toSiteRow(site),
                runtimeDefinition == null ? null : runtimeDefinition.getRuntimeNamespace(),
                runtimeDefinition == null ? null : runtimeDefinition.getVirtualMachineName(),
                runtimeDefinition == null ? null : runtimeDefinition.getServiceName(),
                runtimeDefinition == null ? null : runtimeDefinition.getIngressName(),
                runtimeStatus == null ? "UNKNOWN" : runtimeStatus.runtimePhase(),
                runtimeStatus == null ? "No runtime status loaded." : runtimeStatus.message(),
                domains.stream().map(domain -> new PlatformAdminViewModels.AdminDomainRow(domain.getHost(), domain.getDomainStatus().name(), domain.isPrimaryDomain())).toList(),
                publications.stream().map(publication -> new PlatformAdminViewModels.AdminPublicationRow(
                        publication.getVersionLabel(),
                        publication.getPublicationStatus().name(),
                        publication.getBuildReference(),
                        publication.getPublishedAt()
                )).toList(),
                deployments.stream().map(this::toDeploymentRow).toList(),
                statusHistory.stream().map(history -> new PlatformAdminViewModels.AdminStatusRow(
                        history.getPreviousState().name(),
                        history.getNewState().name(),
                        history.getTransitionReason(),
                        history.getCreatedAt()
                )).toList(),
                mutations.stream().map(mutation -> new PlatformAdminViewModels.AdminMutationRow(
                        mutation.getCreatedAt(),
                        mutation.getMutationStatus().name(),
                        mutation.getRequestedResources()
                )).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<PlatformAdminViewModels.AdminDeploymentRow> loadDeploymentRows() {
        return siteDeploymentRecordRepository.findTop25ByOrderByCreatedAtDesc().stream().map(this::toDeploymentRow).toList();
    }

    @Transactional(readOnly = true)
    public List<PlatformAdminViewModels.AdminJobRow> loadJobRows() {
        return orchestrationJobRepository.findTop50ByOrderByQueuedAtDesc().stream()
                .map(job -> new PlatformAdminViewModels.AdminJobRow(
                        job.getTenantSite().getSiteName(),
                        job.getJobType().name(),
                        job.getJobStatus().name(),
                        job.getRetryCount(),
                        job.getLastErrorMessage(),
                        job.getQueuedAt()
                )).toList();
    }

    @Transactional(readOnly = true)
    public List<PlatformAdminViewModels.AdminAuditRow> loadAuditRows() {
        return adminActionHistoryRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(action -> new PlatformAdminViewModels.AdminAuditRow(
                        action.getActionType().name(),
                        action.getTenantSiteId() == null ? "platform" : action.getTenantSiteId().toString(),
                        action.getActionDetails().toString(),
                        action.getCreatedAt()
                )).toList();
    }

    private PlatformAdminViewModels.AdminTenantRow toTenantRow(UUID userId, TenantMembership membership, TenantOnboardingState onboardingState) {
        long siteCount = tenantSiteRepository.findByTenantAccountIdOrderByCreatedAtDesc(membership.getTenantAccount().getId()).size();
        String roles = membership.getPlatformUser().getRoles().stream().map(Enum::name).collect(Collectors.joining(", "));
        return new PlatformAdminViewModels.AdminTenantRow(
                userId,
                membership.getPlatformUser().getDisplayName(),
                membership.getPlatformUser().getEmail(),
                roles,
                membership.getTenantAccount().getId(),
                membership.getTenantAccount().getDisplayName(),
                onboardingState == null ? "UNKNOWN" : onboardingState.getReadinessState().name(),
                siteCount,
                membership.getPlatformUser().getCreatedAt()
        );
    }

    private PlatformAdminViewModels.AdminSiteRow toSiteRow(TenantSite site) {
        SiteRuntimeDefinition runtimeDefinition = siteRuntimeDefinitionRepository.findByTenantSiteId(site.getId()).orElse(null);
        String resources = runtimeDefinition == null
                ? "Unassigned"
                : runtimeDefinition.getDesiredCpuCores() + " CPU / " + runtimeDefinition.getDesiredMemoryMiB() + " MiB / " + runtimeDefinition.getDesiredStorageGiB() + " GiB";
        String primaryDomain = siteDomainRepository.findByTenantSiteIdOrderByPrimaryDomainDescCreatedAtAsc(site.getId()).stream()
                .filter(SiteDomain::isPrimaryDomain)
                .map(SiteDomain::getHost)
                .findFirst()
                .orElse("Unassigned");
        String publicationVersion = site.getCurrentPublicationId() == null
                ? "None"
                : sitePublicationRepository.findById(site.getCurrentPublicationId()).map(SitePublication::getVersionLabel).orElse("Missing");
        return new PlatformAdminViewModels.AdminSiteRow(
                site.getId(),
                site.getSiteName(),
                site.getSlug(),
                site.getTenantAccount().getDisplayName(),
                site.getLifecycleState().name(),
                site.getReadinessState().name(),
                resources,
                primaryDomain,
                publicationVersion,
                site.getUpdatedAt()
        );
    }

    private PlatformAdminViewModels.AdminDeploymentRow toDeploymentRow(SiteDeploymentRecord deploymentRecord) {
        return new PlatformAdminViewModels.AdminDeploymentRow(
                deploymentRecord.getTenantSite().getSiteName(),
                deploymentRecord.getActionType().name(),
                deploymentRecord.getDeploymentStatus().name(),
                deploymentRecord.getErrorMessage(),
                deploymentRecord.getSitePublication() == null ? "n/a" : deploymentRecord.getSitePublication().getVersionLabel(),
                deploymentRecord.getCreatedAt()
        );
    }
}
