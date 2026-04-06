package org.tavall.platform.control.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.tavall.platform.control.config.ControlPlaneProperties;
import org.tavall.platform.core.AdminActionType;
import org.tavall.platform.core.OrchestrationJobType;
import org.tavall.platform.core.SiteDeploymentStatus;
import org.tavall.platform.core.SiteDomainStatus;
import org.tavall.platform.core.SitePublicationStatus;
import org.tavall.platform.core.TenantReadinessState;
import org.tavall.platform.core.TenantSiteLifecycleState;
import org.tavall.platform.core.command.AssignTenantSiteDomainCommand;
import org.tavall.platform.core.command.MutateTenantSiteInfrastructureProfileCommand;
import org.tavall.platform.core.command.MutateTenantSiteRuntimeResourcesCommand;
import org.tavall.platform.core.command.PublishTenantSiteVersionCommand;
import org.tavall.platform.core.exception.PlatformDomainException;
import org.tavall.platform.persistence.entity.OrchestrationJob;
import org.tavall.platform.persistence.entity.RuntimeMutationHistory;
import org.tavall.platform.persistence.entity.SiteDeploymentRecord;
import org.tavall.platform.persistence.entity.SiteDomain;
import org.tavall.platform.persistence.entity.SitePublication;
import org.tavall.platform.persistence.entity.SiteRuntimeDefinition;
import org.tavall.platform.persistence.entity.TenantSite;
import org.tavall.platform.persistence.repository.RuntimeMutationHistoryRepository;
import org.tavall.platform.persistence.repository.SiteDomainRepository;
import org.tavall.platform.persistence.repository.SitePublicationRepository;
import org.tavall.platform.persistence.repository.SiteRuntimeDefinitionRepository;
import org.tavall.platform.persistence.repository.TenantSiteRepository;
import org.tavall.platform.runtime.TenantSiteDeploymentRequest;
import org.tavall.platform.runtime.TenantSiteDeploymentResult;
import org.tavall.platform.runtime.KubeVirtClusterCompatibilityReport;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;
import org.tavall.platform.runtime.TenantSiteRuntimePowerState;
import org.tavall.platform.runtime.TenantSiteRuntimeResources;
import org.tavall.platform.runtime.TenantSiteRuntimeSpec;
import org.tavall.platform.runtime.TenantSiteRuntimeStatus;

@Service
public class TenantSiteRuntimeControlService {

    private final TenantSiteRepository tenantSiteRepository;
    private final SiteRuntimeDefinitionRepository runtimeDefinitionRepository;
    private final SitePublicationRepository publicationRepository;
    private final SiteDomainRepository siteDomainRepository;
    private final RuntimeMutationHistoryRepository runtimeMutationHistoryRepository;
    private final SiteLifecyclePersistenceService lifecyclePersistenceService;
    private final TenantSiteRuntimeProvisioner runtimeProvisioner;
    private final TenantSiteRuntimeSpecFactory runtimeSpecFactory;
    private final ControlPlaneProperties controlPlaneProperties;

    public TenantSiteRuntimeControlService(
            TenantSiteRepository tenantSiteRepository,
            SiteRuntimeDefinitionRepository runtimeDefinitionRepository,
            SitePublicationRepository publicationRepository,
            SiteDomainRepository siteDomainRepository,
            RuntimeMutationHistoryRepository runtimeMutationHistoryRepository,
            SiteLifecyclePersistenceService lifecyclePersistenceService,
            TenantSiteRuntimeProvisioner runtimeProvisioner,
            TenantSiteRuntimeSpecFactory runtimeSpecFactory,
            ControlPlaneProperties controlPlaneProperties
    ) {
        this.tenantSiteRepository = tenantSiteRepository;
        this.runtimeDefinitionRepository = runtimeDefinitionRepository;
        this.publicationRepository = publicationRepository;
        this.siteDomainRepository = siteDomainRepository;
        this.runtimeMutationHistoryRepository = runtimeMutationHistoryRepository;
        this.lifecyclePersistenceService = lifecyclePersistenceService;
        this.runtimeProvisioner = runtimeProvisioner;
        this.runtimeSpecFactory = runtimeSpecFactory;
        this.controlPlaneProperties = controlPlaneProperties;
    }

    public SiteRuntimeDefinition createTenantSiteRuntime(UUID siteId, UUID requestedByUserId) {
        TenantSite site = loadSite(siteId);
        return runtimeDefinitionRepository.findByTenantSiteId(siteId)
                .orElseGet(() -> {
                    SiteRuntimeDefinition runtimeDefinition = new SiteRuntimeDefinition();
                    runtimeDefinition.setTenantSite(site);
                    String suffix = site.getId().toString().replace("-", "").substring(0, 8);
                    runtimeDefinition.setRuntimeNamespace(controlPlaneProperties.getKubernetes().getNamespacePrefix() + "-" + site.getSlug() + "-" + suffix);
                    runtimeDefinition.setVirtualMachineName(site.getSlug() + "-" + suffix);
                    runtimeDefinition.setServiceName(site.getSlug() + "-svc");
                    runtimeDefinition.setIngressName(site.getSlug() + "-ing");
                    TenantSiteRuntimeResources resources = initialResources(site);
                    runtimeDefinition.setDesiredCpuCores(resources.cpuCores());
                    runtimeDefinition.setDesiredMemoryMiB(resources.memoryMiB());
                    runtimeDefinition.setDesiredStorageGiB(resources.storageGiB());
                    Map<String, Object> desiredConfig = new LinkedHashMap<>(site.getSiteConfiguration());
                    desiredConfig.putIfAbsent("databaseSchema", runtimeSpecFactory.defaultDatabaseSchema(site.getId(), site.getSlug()));
                    desiredConfig.putIfAbsent("runtimeBaseImage", controlPlaneProperties.getKubernetes().getRuntimeBaseImage());
                    desiredConfig.putIfAbsent("infrastructureProfile", controlPlaneProperties.getKubernetes().getInfrastructureProfile().name());
                    desiredConfig.putIfAbsent("bootstrapArtifactUrl", controlPlaneProperties.getTenantRuntime().getBootstrapArtifactUrl());
                    desiredConfig.putIfAbsent("bootstrapArtifactSha256", controlPlaneProperties.getTenantRuntime().getBootstrapArtifactSha256());
                    runtimeDefinition.setDesiredConfig(desiredConfig);
                    runtimeDefinition.setActualConfig(new LinkedHashMap<>());
                    runtimeDefinition.setStatusPayload(new LinkedHashMap<>());
                    SiteRuntimeDefinition saved = runtimeDefinitionRepository.save(runtimeDefinition);
                    lifecyclePersistenceService.audit(
                            requestedByUserId,
                            "tenant-site",
                            site.getId().toString(),
                            "runtime.created",
                            "Tenant site runtime definition created.",
                            Map.of("runtimeNamespace", saved.getRuntimeNamespace(), "virtualMachineName", saved.getVirtualMachineName())
                    );
                    return saved;
                });
    }

    public SitePublication publishTenantSiteVersion(PublishTenantSiteVersionCommand command) {
        TenantSite site = loadSite(command.siteId());
        publicationRepository.findByTenantSiteIdOrderByCreatedAtDesc(site.getId()).stream()
                .filter(publication -> publication.getPublicationStatus() == SitePublicationStatus.PUBLISHED)
                .forEach(publication -> {
                    publication.setPublicationStatus(SitePublicationStatus.SUPERSEDED);
                    publicationRepository.save(publication);
                });

        SitePublication publication = new SitePublication();
        publication.setTenantSite(site);
        publication.setCreatedByUserId(command.requestedByUserId());
        publication.setVersionLabel(command.versionLabel());
        publication.setBuildReference(command.buildReference());
        publication.setPublicationStatus(SitePublicationStatus.PUBLISHED);
        publication.setRuntimeConfigSnapshot(copyMap(command.runtimeConfigSnapshot()));
        publication.setStoreConfigSnapshot(copyMap(command.storeConfigSnapshot()));
        publication.setMetadata(copyMap(command.metadata()));
        publication.setPublishedAt(Instant.now());
        SitePublication savedPublication = publicationRepository.save(publication);

        site.setCurrentPublicationId(savedPublication.getId());
        site.setReadinessState(TenantReadinessState.READY_TO_LAUNCH);
        if (site.getLifecycleState() == TenantSiteLifecycleState.DRAFT) {
            lifecyclePersistenceService.transitionSite(site, TenantSiteLifecycleState.READY_TO_LAUNCH, "Initial publication created", "control-api", Map.of("publicationId", savedPublication.getId()));
        }
        tenantSiteRepository.save(site);
        lifecyclePersistenceService.audit(
                command.requestedByUserId(),
                "tenant-site",
                site.getId().toString(),
                "publication.published",
                "Tenant site publication published.",
                Map.of("publicationId", savedPublication.getId(), "versionLabel", savedPublication.getVersionLabel())
        );
        return savedPublication;
    }

    public SiteDomain assignTenantSiteDomain(AssignTenantSiteDomainCommand command) {
        TenantSite site = loadSite(command.siteId());
        if (command.primaryDomain()) {
            siteDomainRepository.findByTenantSiteIdOrderByPrimaryDomainDescCreatedAtAsc(site.getId())
                    .forEach(domain -> {
                        domain.setPrimaryDomain(false);
                        siteDomainRepository.save(domain);
                    });
        }
        SiteDomain domain = siteDomainRepository.findByHostIgnoreCase(command.host()).orElseGet(SiteDomain::new);
        domain.setTenantSite(site);
        domain.setHost(command.host().toLowerCase());
        domain.setPrimaryDomain(command.primaryDomain());
        domain.setDomainStatus(SiteDomainStatus.ASSIGNED);
        domain.setRoutingConfig(Map.of("ingressClass", controlPlaneProperties.getKubernetes().getIngressClassName()));
        SiteDomain savedDomain = siteDomainRepository.save(domain);
        lifecyclePersistenceService.audit(
                command.requestedByUserId(),
                "tenant-site",
                site.getId().toString(),
                "domain.assigned",
                "Tenant site domain assigned.",
                Map.of("host", savedDomain.getHost())
        );
        return savedDomain;
    }

    public TenantSite markTenantSiteReady(UUID siteId, UUID requestedByUserId) {
        TenantSite site = loadSite(siteId);
        site.setReadinessState(TenantReadinessState.READY_TO_LAUNCH);
        if (site.getLifecycleState() == TenantSiteLifecycleState.DRAFT) {
            lifecyclePersistenceService.transitionSite(site, TenantSiteLifecycleState.READY_TO_LAUNCH, "Site marked ready for launch", "control-api", Map.of("requestedByUserId", requestedByUserId));
        }
        lifecyclePersistenceService.audit(
                requestedByUserId,
                "tenant-site",
                site.getId().toString(),
                "site.mark-ready",
                "Tenant site marked ready to launch.",
                Map.of()
        );
        return tenantSiteRepository.save(site);
    }

    public TenantSiteDeploymentResult launchTenantSiteRuntime(UUID siteId, UUID requestedByUserId) {
        TenantSite site = loadSite(siteId);
        SiteRuntimeDefinition runtimeDefinition = createTenantSiteRuntime(siteId, requestedByUserId);
        SitePublication publication = ensurePublication(site, requestedByUserId);
        SiteDomain primaryDomain = ensurePrimaryDomain(site, requestedByUserId);
        TenantSiteRuntimeSpec runtimeSpec = buildTenantSiteRuntimeSpec(siteId);

        OrchestrationJob job = lifecyclePersistenceService.beginJob(site, requestedByUserId, OrchestrationJobType.LAUNCH_RUNTIME, Map.of("siteId", siteId));
        SiteDeploymentRecord deploymentRecord = recordTenantSiteDeployment(site, publication, job, requestedByUserId, AdminActionType.LAUNCH_SITE, serializeRuntimeSpec(runtimeSpec));
        lifecyclePersistenceService.transitionSite(site, TenantSiteLifecycleState.PROVISIONING, "Launch requested", "control-api", Map.of("jobId", job.getId()));

        TenantSiteDeploymentResult deploymentResult = runtimeProvisioner.createOrUpdateRuntime(new TenantSiteDeploymentRequest(
                site.getId(),
                publication.getId(),
                requestedByUserId,
                "launch",
                runtimeSpec,
                Map.of("siteSlug", site.getSlug(), "domain", primaryDomain.getHost())
        ));

        applyDeploymentOutcome(site, runtimeDefinition, job, deploymentRecord, requestedByUserId, AdminActionType.LAUNCH_SITE, deploymentResult, runtimeSpec);
        return deploymentResult;
    }

    public TenantSiteRuntimeStatus loadTenantSiteRuntimeStatus(UUID siteId) {
        return runtimeProvisioner.loadRuntimeStatus(buildTenantSiteRuntimeSpec(siteId));
    }

    public TenantSiteRuntimeStatus stopTenantSiteRuntime(UUID siteId, UUID requestedByUserId) {
        return executeRuntimePowerOperation(siteId, requestedByUserId, OrchestrationJobType.STOP_RUNTIME, AdminActionType.STOP_SITE, TenantSiteLifecycleState.STOPPED, runtimeProvisioner::stopRuntime);
    }

    public TenantSiteRuntimeStatus restartTenantSiteRuntime(UUID siteId, UUID requestedByUserId) {
        return executeRuntimePowerOperation(siteId, requestedByUserId, OrchestrationJobType.RESTART_RUNTIME, AdminActionType.RESTART_SITE, TenantSiteLifecycleState.RUNNING, runtimeProvisioner::restartRuntime);
    }

    public TenantSiteRuntimeStatus destroyTenantSiteRuntime(UUID siteId, UUID requestedByUserId) {
        TenantSite site = loadSite(siteId);
        TenantSiteRuntimeSpec runtimeSpec = buildTenantSiteRuntimeSpec(siteId);
        OrchestrationJob job = lifecyclePersistenceService.beginJob(site, requestedByUserId, OrchestrationJobType.DESTROY_RUNTIME, Map.of("siteId", siteId));
        SiteDeploymentRecord deploymentRecord = recordTenantSiteDeployment(site, null, job, requestedByUserId, AdminActionType.DESTROY_SITE, serializeRuntimeSpec(runtimeSpec));
        lifecyclePersistenceService.transitionSite(site, TenantSiteLifecycleState.DESTROYING, "Destroy requested", "control-api", Map.of("jobId", job.getId()));
        TenantSiteRuntimeStatus runtimeStatus = runtimeProvisioner.destroyRuntime(runtimeSpec);
        if (runtimeStatus.powerState() == TenantSiteRuntimePowerState.DESTROYED) {
            site.setDestroyedAt(Instant.now());
            lifecyclePersistenceService.transitionSite(site, TenantSiteLifecycleState.DESTROYED, "Destroy completed", "control-api", Map.of("jobId", job.getId()));
            lifecyclePersistenceService.completeJobSuccess(job, copyMap(runtimeStatus.details()));
            lifecyclePersistenceService.markTenantSiteDeploymentSucceeded(
                    deploymentRecord,
                    new TenantSiteDeploymentResult(true, runtimeSpec.virtualMachineName(), runtimeStatus.message(), runtimeStatus, copyMap(runtimeStatus.details()), Instant.now())
            );
        } else {
            lifecyclePersistenceService.transitionSite(site, TenantSiteLifecycleState.FAILED, "Destroy failed", "control-api", copyMap(runtimeStatus.details()));
            lifecyclePersistenceService.completeJobFailure(job, runtimeStatus.message(), copyMap(runtimeStatus.details()));
            markTenantSiteDeploymentFailed(deploymentRecord, runtimeStatus.message(), copyMap(runtimeStatus.details()));
        }
        tenantSiteRepository.save(site);
        return runtimeStatus;
    }

    public TenantSiteRuntimeStatus mutateTenantSiteRuntimeResources(MutateTenantSiteRuntimeResourcesCommand command) {
        TenantSite site = loadSite(command.siteId());
        SiteRuntimeDefinition runtimeDefinition = createTenantSiteRuntime(command.siteId(), command.requestedByUserId());
        Map<String, Object> previousResources = currentResourceMap(runtimeDefinition);
        runtimeDefinition.setDesiredCpuCores(command.resources().cpuCores());
        runtimeDefinition.setDesiredMemoryMiB(command.resources().memoryMiB());
        runtimeDefinition.setDesiredStorageGiB(command.resources().storageGiB());
        runtimeDefinitionRepository.save(runtimeDefinition);

        RuntimeMutationHistory mutationHistory = new RuntimeMutationHistory();
        mutationHistory.setTenantSite(site);
        mutationHistory.setRequestedByUserId(command.requestedByUserId());
        mutationHistory.setPreviousResources(previousResources);
        mutationHistory.setRequestedResources(currentResourceMap(runtimeDefinition));
        mutationHistory.setMutationStatus(SiteDeploymentStatus.IN_PROGRESS);
        runtimeMutationHistoryRepository.save(mutationHistory);

        SitePublication publication = ensurePublication(site, command.requestedByUserId());
        OrchestrationJob job = lifecyclePersistenceService.beginJob(site, command.requestedByUserId(), OrchestrationJobType.MUTATE_RESOURCES, Map.of("siteId", site.getId()));
        SiteDeploymentRecord deploymentRecord = recordTenantSiteDeployment(site, publication, job, command.requestedByUserId(), AdminActionType.MUTATE_SITE_RESOURCES, serializeRuntimeSpec(buildTenantSiteRuntimeSpec(site.getId())));
        lifecyclePersistenceService.transitionSite(site, mutationRequestedState(site), "Resource mutation requested", "control-api", currentResourceMap(runtimeDefinition));

        TenantSiteRuntimeSpec runtimeSpec = buildTenantSiteRuntimeSpec(site.getId());
        TenantSiteDeploymentResult deploymentResult = runtimeProvisioner.createOrUpdateRuntime(new TenantSiteDeploymentRequest(
                site.getId(),
                publication.getId(),
                command.requestedByUserId(),
                "mutate-resources",
                runtimeSpec,
                Map.of("resourceMutation", true)
        ));
        applyDeploymentOutcome(site, runtimeDefinition, job, deploymentRecord, command.requestedByUserId(), AdminActionType.MUTATE_SITE_RESOURCES, deploymentResult, runtimeSpec);
        mutationHistory.setMutationStatus(deploymentResult.successful() ? SiteDeploymentStatus.SUCCEEDED : SiteDeploymentStatus.FAILED);
        mutationHistory.setResultPayload(copyMap(deploymentResult.details()));
        runtimeMutationHistoryRepository.save(mutationHistory);
        return deploymentResult.runtimeStatus();
    }

    public TenantSiteRuntimeStatus mutateTenantSiteInfrastructureProfile(MutateTenantSiteInfrastructureProfileCommand command) {
        TenantSite site = loadSite(command.siteId());
        SiteRuntimeDefinition runtimeDefinition = createTenantSiteRuntime(command.siteId(), command.requestedByUserId());
        Map<String, Object> previousInfrastructureState = currentInfrastructureMap(site, runtimeDefinition);
        runtimeDefinition.getDesiredConfig().put("infrastructureProfile", command.infrastructureProfile().name());
        runtimeDefinitionRepository.save(runtimeDefinition);

        RuntimeMutationHistory mutationHistory = new RuntimeMutationHistory();
        mutationHistory.setTenantSite(site);
        mutationHistory.setRequestedByUserId(command.requestedByUserId());
        mutationHistory.setPreviousResources(previousInfrastructureState);
        mutationHistory.setRequestedResources(currentInfrastructureMap(site, runtimeDefinition));
        mutationHistory.setMutationStatus(SiteDeploymentStatus.IN_PROGRESS);
        runtimeMutationHistoryRepository.save(mutationHistory);

        SitePublication publication = ensurePublication(site, command.requestedByUserId());
        OrchestrationJob job = lifecyclePersistenceService.beginJob(site, command.requestedByUserId(), OrchestrationJobType.MUTATE_INFRASTRUCTURE_PROFILE, Map.of("siteId", site.getId()));
        SiteDeploymentRecord deploymentRecord = recordTenantSiteDeployment(site, publication, job, command.requestedByUserId(), AdminActionType.MUTATE_SITE_INFRASTRUCTURE_PROFILE, serializeRuntimeSpec(buildTenantSiteRuntimeSpec(site.getId())));
        lifecyclePersistenceService.transitionSite(site, mutationRequestedState(site), "Infrastructure profile mutation requested", "control-api", currentInfrastructureMap(site, runtimeDefinition));

        TenantSiteRuntimeSpec runtimeSpec = buildTenantSiteRuntimeSpec(site.getId());
        TenantSiteDeploymentResult deploymentResult = runtimeProvisioner.createOrUpdateRuntime(new TenantSiteDeploymentRequest(
                site.getId(),
                publication.getId(),
                command.requestedByUserId(),
                "mutate-infrastructure-profile",
                runtimeSpec,
                Map.of("infrastructureProfile", command.infrastructureProfile().name())
        ));
        applyDeploymentOutcome(site, runtimeDefinition, job, deploymentRecord, command.requestedByUserId(), AdminActionType.MUTATE_SITE_INFRASTRUCTURE_PROFILE, deploymentResult, runtimeSpec);
        mutationHistory.setMutationStatus(deploymentResult.successful() ? SiteDeploymentStatus.SUCCEEDED : SiteDeploymentStatus.FAILED);
        mutationHistory.setResultPayload(copyMap(deploymentResult.details()));
        runtimeMutationHistoryRepository.save(mutationHistory);
        return deploymentResult.runtimeStatus();
    }

    public TenantSiteRuntimeStatus synchronizeTenantSiteRuntimeStatus(UUID siteId, UUID requestedByUserId) {
        TenantSite site = loadSite(siteId);
        TenantSiteRuntimeStatus runtimeStatus = runtimeProvisioner.loadRuntimeStatus(buildTenantSiteRuntimeSpec(siteId));
        SiteRuntimeDefinition runtimeDefinition = runtimeDefinitionRepository.findByTenantSiteId(siteId)
                .orElseThrow(() -> new PlatformDomainException("No runtime definition exists for site " + siteId));
        runtimeDefinition.setStatusPayload(copyMap(runtimeStatus.details()));
        runtimeDefinition.setLastSynchronizedAt(runtimeStatus.observedAt());
        runtimeDefinitionRepository.save(runtimeDefinition);
        TenantSiteLifecycleState mappedState = mapSiteLifecycleState(runtimeStatus.powerState(), site.getLifecycleState());
        if (mappedState != site.getLifecycleState()) {
            lifecyclePersistenceService.transitionSite(site, mappedState, "Runtime status synchronized", "control-api", copyMap(runtimeStatus.details()));
        }
        lifecyclePersistenceService.audit(
                requestedByUserId,
                "tenant-site",
                site.getId().toString(),
                "runtime.synchronized",
                "Tenant site runtime status synchronized.",
                Map.of("runtimePhase", runtimeStatus.runtimePhase())
        );
        return runtimeStatus;
    }

    public TenantSiteRuntimeSpec buildTenantSiteRuntimeSpec(UUID siteId) {
        TenantSite site = loadSite(siteId);
        SiteRuntimeDefinition runtimeDefinition = runtimeDefinitionRepository.findByTenantSiteId(siteId)
                .orElseThrow(() -> new PlatformDomainException("No runtime definition exists for site " + siteId));
        SitePublication publication = ensurePublication(site, site.getCreatedByUserId());
        SiteDomain primaryDomain = ensurePrimaryDomain(site, site.getCreatedByUserId());
        return runtimeSpecFactory.buildTenantSiteRuntimeSpec(site, runtimeDefinition, publication, primaryDomain.getHost());
    }

    public TenantRuntimeInfrastructureProfile loadInfrastructureProfile(UUID siteId) {
        return buildTenantSiteRuntimeSpec(siteId).infrastructureProfile();
    }

    public KubeVirtClusterCompatibilityReport loadClusterCompatibility(UUID siteId) {
        return runtimeProvisioner.loadClusterCompatibility(buildTenantSiteRuntimeSpec(siteId));
    }

    public SiteDeploymentRecord recordTenantSiteDeployment(
            TenantSite site,
            SitePublication publication,
            OrchestrationJob job,
            UUID actorUserId,
            AdminActionType actionType,
            Map<String, Object> runtimeSpecSnapshot
    ) {
        return lifecyclePersistenceService.recordTenantSiteDeployment(site, publication, job, actorUserId, actionType, runtimeSpecSnapshot);
    }

    public void markTenantSiteDeploymentFailed(SiteDeploymentRecord deploymentRecord, String errorMessage, Map<String, Object> resultPayload) {
        lifecyclePersistenceService.markTenantSiteDeploymentFailed(deploymentRecord, errorMessage, resultPayload);
    }

    private TenantSite loadSite(UUID siteId) {
        return tenantSiteRepository.findById(siteId)
                .orElseThrow(() -> new PlatformDomainException("Tenant site " + siteId + " was not found."));
    }

    private SitePublication ensurePublication(TenantSite site, UUID actorUserId) {
        if (site.getCurrentPublicationId() != null) {
            return publicationRepository.findById(site.getCurrentPublicationId())
                    .orElseThrow(() -> new PlatformDomainException("Current publication " + site.getCurrentPublicationId() + " was not found."));
        }
        return publicationRepository.findFirstByTenantSiteIdAndPublicationStatusOrderByCreatedAtDesc(site.getId(), SitePublicationStatus.PUBLISHED)
                .orElseGet(() -> publishTenantSiteVersion(new PublishTenantSiteVersionCommand(
                        site.getId(),
                        actorUserId,
                        "v1-initial",
                        "webstore-view:latest",
                        Map.of(
                                "runtimeBaseImage", controlPlaneProperties.getKubernetes().getRuntimeBaseImage(),
                                "infrastructureProfile", controlPlaneProperties.getKubernetes().getInfrastructureProfile().name(),
                                "bootstrapArtifactUrl", controlPlaneProperties.getTenantRuntime().getBootstrapArtifactUrl(),
                                "bootstrapArtifactSha256", controlPlaneProperties.getTenantRuntime().getBootstrapArtifactSha256()
                        ),
                        copyMap(site.getSiteConfiguration()),
                        Map.of("autoPublished", true)
                )));
    }

    private SiteDomain ensurePrimaryDomain(TenantSite site, UUID actorUserId) {
        return siteDomainRepository.findByTenantSiteIdOrderByPrimaryDomainDescCreatedAtAsc(site.getId()).stream()
                .filter(SiteDomain::isPrimaryDomain)
                .findFirst()
                .orElseGet(() -> assignTenantSiteDomain(new AssignTenantSiteDomainCommand(
                        site.getId(),
                        actorUserId,
                        site.getSlug() + "." + controlPlaneProperties.getRouting().getPublicDomainSuffix(),
                        true
                )));
    }

    private TenantSiteRuntimeResources initialResources(TenantSite site) {
        int cpu = parseInteger(((Map<?, ?>) site.getSiteConfiguration().getOrDefault("runtimeResources", Map.of())).get("cpuCores"), 2);
        int memory = parseInteger(((Map<?, ?>) site.getSiteConfiguration().getOrDefault("runtimeResources", Map.of())).get("memoryMiB"), 2048);
        int storage = parseInteger(((Map<?, ?>) site.getSiteConfiguration().getOrDefault("runtimeResources", Map.of())).get("storageGiB"), 30);
        return new TenantSiteRuntimeResources(cpu, memory, storage);
    }

    private int parseInteger(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private TenantSiteRuntimeStatus executeRuntimePowerOperation(
            UUID siteId,
            UUID requestedByUserId,
            OrchestrationJobType jobType,
            AdminActionType adminActionType,
            TenantSiteLifecycleState successfulState,
            java.util.function.Function<TenantSiteRuntimeSpec, TenantSiteRuntimeStatus> operation
    ) {
        TenantSite site = loadSite(siteId);
        TenantSiteRuntimeSpec runtimeSpec = buildTenantSiteRuntimeSpec(siteId);
        OrchestrationJob job = lifecyclePersistenceService.beginJob(site, requestedByUserId, jobType, Map.of("siteId", siteId));
        SiteDeploymentRecord deploymentRecord = recordTenantSiteDeployment(site, null, job, requestedByUserId, adminActionType, serializeRuntimeSpec(runtimeSpec));
        if (adminActionType == AdminActionType.RESTART_SITE) {
            lifecyclePersistenceService.transitionSite(site, TenantSiteLifecycleState.UPDATING, "Restart requested", "control-api", Map.of("jobId", job.getId()));
        }
        TenantSiteRuntimeStatus runtimeStatus = operation.apply(runtimeSpec);
        if (runtimeStatus.powerState() == TenantSiteRuntimePowerState.FAILED) {
            lifecyclePersistenceService.transitionSite(site, TenantSiteLifecycleState.FAILED, runtimeStatus.message(), "control-api", copyMap(runtimeStatus.details()));
            lifecyclePersistenceService.completeJobFailure(job, runtimeStatus.message(), copyMap(runtimeStatus.details()));
            markTenantSiteDeploymentFailed(deploymentRecord, runtimeStatus.message(), copyMap(runtimeStatus.details()));
        } else {
            lifecyclePersistenceService.transitionSite(site, successfulState, adminActionType.name(), "control-api", copyMap(runtimeStatus.details()));
            lifecyclePersistenceService.completeJobSuccess(job, copyMap(runtimeStatus.details()));
            lifecyclePersistenceService.markTenantSiteDeploymentSucceeded(
                    deploymentRecord,
                    new TenantSiteDeploymentResult(true, runtimeSpec.virtualMachineName(), runtimeStatus.message(), runtimeStatus, copyMap(runtimeStatus.details()), Instant.now())
            );
        }
        return runtimeStatus;
    }

    private void applyDeploymentOutcome(
            TenantSite site,
            SiteRuntimeDefinition runtimeDefinition,
            OrchestrationJob job,
            SiteDeploymentRecord deploymentRecord,
            UUID requestedByUserId,
            AdminActionType actionType,
            TenantSiteDeploymentResult deploymentResult,
            TenantSiteRuntimeSpec runtimeSpec
    ) {
        runtimeDefinition.setRuntimeIdentifier(deploymentResult.deploymentReference());
        Map<String, Object> actualConfig = new LinkedHashMap<>();
        actualConfig.put("publicationVersion", runtimeSpec.publicationVersion());
        actualConfig.put("baseImage", runtimeSpec.baseImage());
        actualConfig.put("primaryDomain", runtimeSpec.primaryDomain());
        actualConfig.put("infrastructureProfile", runtimeSpec.infrastructureProfile().name());
        actualConfig.put("nodeSelector", new LinkedHashMap<>(runtimeSpec.nodeSelector()));
        runtimeDefinition.setActualConfig(actualConfig);
        runtimeDefinition.setStatusPayload(copyMap(deploymentResult.runtimeStatus().details()));
        runtimeDefinition.setLastSynchronizedAt(deploymentResult.completedAt());
        runtimeDefinitionRepository.save(runtimeDefinition);

        if (deploymentResult.successful()) {
            lifecyclePersistenceService.completeJobSuccess(job, copyMap(deploymentResult.details()));
            lifecyclePersistenceService.markTenantSiteDeploymentSucceeded(deploymentRecord, deploymentResult);
            TenantSiteLifecycleState nextState = mapSiteLifecycleState(deploymentResult.runtimeStatus().powerState(), site.getLifecycleState());
            lifecyclePersistenceService.transitionSite(site, nextState, deploymentResult.message(), "control-api", copyMap(deploymentResult.details()));
            if (nextState == TenantSiteLifecycleState.RUNNING) {
                site.setLaunchedAt(Instant.now());
                tenantSiteRepository.save(site);
            }
        } else {
            lifecyclePersistenceService.completeJobFailure(job, deploymentResult.message(), copyMap(deploymentResult.details()));
            markTenantSiteDeploymentFailed(deploymentRecord, deploymentResult.message(), copyMap(deploymentResult.details()));
            lifecyclePersistenceService.transitionSite(site, TenantSiteLifecycleState.FAILED, deploymentResult.message(), "control-api", copyMap(deploymentResult.details()));
            site.setLastErrorMessage(deploymentResult.message());
            tenantSiteRepository.save(site);
        }
        lifecyclePersistenceService.recordAdminAction(
                requestedByUserId,
                site,
                actionType,
                Map.of("siteId", site.getId()),
                Map.of("siteId", site.getId(), "state", site.getLifecycleState().name()),
                Map.of("deploymentReference", deploymentResult.deploymentReference())
        );
    }

    private Map<String, Object> currentResourceMap(SiteRuntimeDefinition runtimeDefinition) {
        return Map.of(
                "cpuCores", runtimeDefinition.getDesiredCpuCores(),
                "memoryMiB", runtimeDefinition.getDesiredMemoryMiB(),
                "storageGiB", runtimeDefinition.getDesiredStorageGiB()
        );
    }

    private Map<String, Object> currentInfrastructureMap(TenantSite site, SiteRuntimeDefinition runtimeDefinition) {
        TenantSiteRuntimeSpec runtimeSpec = runtimeSpecFactory.buildTenantSiteRuntimeSpec(
                site,
                runtimeDefinition,
                ensurePublication(site, site.getCreatedByUserId()),
                ensurePrimaryDomain(site, site.getCreatedByUserId()).getHost()
        );
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("siteId", site.getId());
        state.put("infrastructureProfile", runtimeSpec.infrastructureProfile().name());
        state.put("nodeSelector", new LinkedHashMap<>(runtimeSpec.nodeSelector()));
        return state;
    }

    private TenantSiteLifecycleState mutationRequestedState(TenantSite site) {
        return switch (site.getLifecycleState()) {
            case DRAFT, READY_TO_LAUNCH, FAILED -> TenantSiteLifecycleState.PROVISIONING;
            default -> TenantSiteLifecycleState.UPDATING;
        };
    }

    private TenantSiteLifecycleState mapSiteLifecycleState(TenantSiteRuntimePowerState powerState, TenantSiteLifecycleState currentState) {
        return switch (powerState) {
            case RUNNING -> TenantSiteLifecycleState.RUNNING;
            case STOPPED -> TenantSiteLifecycleState.STOPPED;
            case PROVISIONING -> TenantSiteLifecycleState.PROVISIONING;
            case FAILED -> TenantSiteLifecycleState.FAILED;
            case DESTROYING -> TenantSiteLifecycleState.DESTROYING;
            case DESTROYED -> switch (currentState) {
                case DESTROYING, DESTROYED -> TenantSiteLifecycleState.DESTROYED;
                default -> TenantSiteLifecycleState.FAILED;
            };
            case DRAFT -> TenantSiteLifecycleState.DRAFT;
            case UNKNOWN -> currentState;
        };
    }

    private Map<String, Object> serializeRuntimeSpec(TenantSiteRuntimeSpec runtimeSpec) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("siteId", runtimeSpec.siteId());
        snapshot.put("tenantId", runtimeSpec.tenantId());
        snapshot.put("namespace", runtimeSpec.namespace());
        snapshot.put("virtualMachineName", runtimeSpec.virtualMachineName());
        snapshot.put("serviceName", runtimeSpec.serviceName());
        snapshot.put("primaryDomain", runtimeSpec.primaryDomain());
        snapshot.put("baseImage", runtimeSpec.baseImage());
        snapshot.put("bootstrapArtifactUrl", runtimeSpec.bootstrapArtifactUrl());
        snapshot.put("publicationVersion", runtimeSpec.publicationVersion());
        snapshot.put("runtimeDatabaseSchema", runtimeSpec.runtimeDatabaseSchema());
        snapshot.put("infrastructureProfile", runtimeSpec.infrastructureProfile().name());
        snapshot.put("nodeSelector", new LinkedHashMap<>(runtimeSpec.nodeSelector()));
        snapshot.put("resources", Map.of(
                "cpuCores", runtimeSpec.resources().cpuCores(),
                "memoryMiB", runtimeSpec.resources().memoryMiB(),
                "storageGiB", runtimeSpec.resources().storageGiB()
        ));
        snapshot.put("environmentKeys", new ArrayList<>(runtimeSpec.environment().keySet()));
        return snapshot;
    }

    private Map<String, Object> copyMap(Map<String, Object> map) {
        return map == null ? new LinkedHashMap<>() : new LinkedHashMap<>(map);
    }
}
