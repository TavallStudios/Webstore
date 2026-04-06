package org.tavall.platform.web.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.platform.auth.PlatformSessionUser;
import org.tavall.platform.core.SiteDomainStatus;
import org.tavall.platform.core.SitePublicationStatus;
import org.tavall.platform.core.TenantReadinessState;
import org.tavall.platform.core.TenantSiteLifecycleState;
import org.tavall.platform.core.exception.PlatformDomainException;
import org.tavall.platform.core.util.Slugifier;
import org.tavall.platform.persistence.entity.SiteDomain;
import org.tavall.platform.persistence.entity.SitePublication;
import org.tavall.platform.persistence.entity.TenantAccount;
import org.tavall.platform.persistence.entity.TenantOnboardingState;
import org.tavall.platform.persistence.entity.TenantSite;
import org.tavall.platform.persistence.repository.SiteDomainRepository;
import org.tavall.platform.persistence.repository.SitePublicationRepository;
import org.tavall.platform.persistence.repository.TenantAccountRepository;
import org.tavall.platform.persistence.repository.TenantOnboardingStateRepository;
import org.tavall.platform.persistence.repository.TenantSiteRepository;
import org.tavall.platform.web.model.PlatformOnboardingForm;

@Service
public class PlatformTenantOnboardingService {

    private final TenantAccountRepository tenantAccountRepository;
    private final TenantOnboardingStateRepository tenantOnboardingStateRepository;
    private final TenantSiteRepository tenantSiteRepository;
    private final SitePublicationRepository sitePublicationRepository;
    private final SiteDomainRepository siteDomainRepository;
    private final Slugifier slugifier;

    public PlatformTenantOnboardingService(
            TenantAccountRepository tenantAccountRepository,
            TenantOnboardingStateRepository tenantOnboardingStateRepository,
            TenantSiteRepository tenantSiteRepository,
            SitePublicationRepository sitePublicationRepository,
            SiteDomainRepository siteDomainRepository,
            Slugifier slugifier
    ) {
        this.tenantAccountRepository = tenantAccountRepository;
        this.tenantOnboardingStateRepository = tenantOnboardingStateRepository;
        this.tenantSiteRepository = tenantSiteRepository;
        this.sitePublicationRepository = sitePublicationRepository;
        this.siteDomainRepository = siteDomainRepository;
        this.slugifier = slugifier;
    }

    @Transactional
    public TenantSite completeOnboarding(PlatformSessionUser sessionUser, PlatformOnboardingForm form) {
        TenantAccount tenantAccount = tenantAccountRepository.findById(sessionUser.activeTenantAccountId())
                .orElseThrow(() -> new PlatformDomainException("Active tenant account was not found."));
        TenantOnboardingState onboardingState = tenantOnboardingStateRepository.findByTenantAccountId(tenantAccount.getId())
                .orElseThrow(() -> new PlatformDomainException("Tenant onboarding state was not found."));

        tenantAccount.setDisplayName(form.getWorkspaceName());
        tenantAccount.setSlug(allocateWorkspaceSlug(form.getWorkspaceName(), tenantAccount.getId().toString()));
        tenantAccountRepository.save(tenantAccount);

        TenantSite site = tenantSiteRepository.findByTenantAccountIdOrderByCreatedAtDesc(tenantAccount.getId()).stream()
                .findFirst()
                .orElseGet(() -> createInitialSite(sessionUser, tenantAccount, form));

        onboardingState.setReadinessState(TenantReadinessState.ACTIVE);
        onboardingState.setCurrentStep("completed");
        onboardingState.setCompletedAt(Instant.now());
        onboardingState.setDraftData(Map.of(
                "workspaceName", form.getWorkspaceName(),
                "siteId", site.getId(),
                "siteSlug", site.getSlug()
        ));
        tenantOnboardingStateRepository.save(onboardingState);
        return site;
    }

    private TenantSite createInitialSite(PlatformSessionUser sessionUser, TenantAccount tenantAccount, PlatformOnboardingForm form) {
        TenantSite site = new TenantSite();
        site.setTenantAccount(tenantAccount);
        site.setCreatedByUserId(sessionUser.platformUserId());
        site.setSiteName(form.getSiteName());
        site.setSlug(slugifier.toSlug(form.getSiteSlug()));
        site.setLifecycleState(TenantSiteLifecycleState.READY_TO_LAUNCH);
        site.setReadinessState(TenantReadinessState.READY_TO_LAUNCH);
        site.setSummary("Initial tenant runtime created from onboarding.");
        site.setSiteConfiguration(buildSiteConfiguration(form));
        TenantSite savedSite = tenantSiteRepository.save(site);

        SitePublication publication = new SitePublication();
        publication.setTenantSite(savedSite);
        publication.setCreatedByUserId(sessionUser.platformUserId());
        publication.setVersionLabel("v1-initial");
        publication.setBuildReference("webstore-view:latest");
        publication.setPublicationStatus(SitePublicationStatus.PUBLISHED);
        publication.setRuntimeConfigSnapshot(Map.of("runtimeBaseImage", "webstore-view:latest"));
        publication.setStoreConfigSnapshot(savedSite.getSiteConfiguration());
        publication.setMetadata(Map.of("createdFrom", "platform-onboarding"));
        publication.setPublishedAt(Instant.now());
        SitePublication savedPublication = sitePublicationRepository.save(publication);

        savedSite.setCurrentPublicationId(savedPublication.getId());
        tenantSiteRepository.save(savedSite);

        SiteDomain siteDomain = new SiteDomain();
        siteDomain.setTenantSite(savedSite);
        siteDomain.setHost(form.getRequestedDomain() == null || form.getRequestedDomain().isBlank()
                ? savedSite.getSlug() + ".stores.local"
                : form.getRequestedDomain().toLowerCase());
        siteDomain.setPrimaryDomain(true);
        siteDomain.setDomainStatus(SiteDomainStatus.ASSIGNED);
        siteDomain.setRoutingConfig(Map.of("source", "platform-onboarding"));
        siteDomainRepository.save(siteDomain);
        return savedSite;
    }

    private Map<String, Object> buildSiteConfiguration(PlatformOnboardingForm form) {
        Map<String, Object> runtimeResources = new LinkedHashMap<>();
        runtimeResources.put("cpuCores", form.getCpuCores());
        runtimeResources.put("memoryMiB", form.getMemoryMiB());
        runtimeResources.put("storageGiB", form.getStorageGiB());
        Map<String, Object> configuration = new LinkedHashMap<>();
        configuration.put("runtimeResources", runtimeResources);
        configuration.put("createdFrom", "onboarding");
        return configuration;
    }

    private String allocateWorkspaceSlug(String workspaceName, String existingId) {
        String baseSlug = slugifier.toSlug(workspaceName);
        String candidate = baseSlug;
        int counter = 1;
        while (tenantAccountRepository.findBySlug(candidate)
                .filter(account -> !account.getId().toString().equals(existingId))
                .isPresent()) {
            counter++;
            candidate = baseSlug + "-" + counter;
        }
        return candidate;
    }
}
