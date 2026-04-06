package org.tavall.platform.control.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.platform.core.AdminActionType;
import org.tavall.platform.core.OrchestrationJobStatus;
import org.tavall.platform.core.OrchestrationJobType;
import org.tavall.platform.core.SiteDeploymentStatus;
import org.tavall.platform.core.TenantSiteLifecycleState;
import org.tavall.platform.core.service.TenantSiteLifecycleTransitions;
import org.tavall.platform.persistence.entity.AdminActionHistory;
import org.tavall.platform.persistence.entity.AuditLogEntry;
import org.tavall.platform.persistence.entity.OrchestrationJob;
import org.tavall.platform.persistence.entity.SiteDeploymentRecord;
import org.tavall.platform.persistence.entity.SitePublication;
import org.tavall.platform.persistence.entity.SiteStatusHistory;
import org.tavall.platform.persistence.entity.TenantSite;
import org.tavall.platform.persistence.repository.AdminActionHistoryRepository;
import org.tavall.platform.persistence.repository.AuditLogEntryRepository;
import org.tavall.platform.persistence.repository.OrchestrationJobRepository;
import org.tavall.platform.persistence.repository.SiteDeploymentRecordRepository;
import org.tavall.platform.persistence.repository.SiteStatusHistoryRepository;
import org.tavall.platform.persistence.repository.TenantSiteRepository;
import org.tavall.platform.runtime.TenantSiteDeploymentResult;

@Service
public class SiteLifecyclePersistenceService {

    private final OrchestrationJobRepository orchestrationJobRepository;
    private final SiteDeploymentRecordRepository siteDeploymentRecordRepository;
    private final SiteStatusHistoryRepository siteStatusHistoryRepository;
    private final TenantSiteRepository tenantSiteRepository;
    private final AdminActionHistoryRepository adminActionHistoryRepository;
    private final AuditLogEntryRepository auditLogEntryRepository;
    private final TenantSiteLifecycleTransitions lifecycleTransitions;

    public SiteLifecyclePersistenceService(
            OrchestrationJobRepository orchestrationJobRepository,
            SiteDeploymentRecordRepository siteDeploymentRecordRepository,
            SiteStatusHistoryRepository siteStatusHistoryRepository,
            TenantSiteRepository tenantSiteRepository,
            AdminActionHistoryRepository adminActionHistoryRepository,
            AuditLogEntryRepository auditLogEntryRepository,
            TenantSiteLifecycleTransitions lifecycleTransitions
    ) {
        this.orchestrationJobRepository = orchestrationJobRepository;
        this.siteDeploymentRecordRepository = siteDeploymentRecordRepository;
        this.siteStatusHistoryRepository = siteStatusHistoryRepository;
        this.tenantSiteRepository = tenantSiteRepository;
        this.adminActionHistoryRepository = adminActionHistoryRepository;
        this.auditLogEntryRepository = auditLogEntryRepository;
        this.lifecycleTransitions = lifecycleTransitions;
    }

    @Transactional
    public OrchestrationJob beginJob(TenantSite site, UUID actorUserId, OrchestrationJobType jobType, Map<String, Object> requestPayload) {
        OrchestrationJob job = new OrchestrationJob();
        job.setTenantSite(site);
        job.setCreatedByUserId(actorUserId);
        job.setJobType(jobType);
        job.setJobStatus(OrchestrationJobStatus.IN_PROGRESS);
        job.setQueuedAt(Instant.now());
        job.setStartedAt(Instant.now());
        job.setRequestPayload(new LinkedHashMap<>(requestPayload));
        return orchestrationJobRepository.save(job);
    }

    @Transactional
    public void completeJobSuccess(OrchestrationJob job, Map<String, Object> resultPayload) {
        job.setJobStatus(OrchestrationJobStatus.SUCCEEDED);
        job.setResultPayload(new LinkedHashMap<>(resultPayload));
        job.setFinishedAt(Instant.now());
        orchestrationJobRepository.save(job);
    }

    @Transactional
    public void completeJobFailure(OrchestrationJob job, String errorMessage, Map<String, Object> resultPayload) {
        job.setJobStatus(OrchestrationJobStatus.FAILED);
        job.setLastErrorMessage(errorMessage);
        job.setResultPayload(new LinkedHashMap<>(resultPayload));
        job.setFinishedAt(Instant.now());
        orchestrationJobRepository.save(job);
    }

    @Transactional
    public SiteDeploymentRecord recordTenantSiteDeployment(
            TenantSite site,
            SitePublication publication,
            OrchestrationJob job,
            UUID actorUserId,
            AdminActionType actionType,
            Map<String, Object> runtimeSpecSnapshot
    ) {
        SiteDeploymentRecord deploymentRecord = new SiteDeploymentRecord();
        deploymentRecord.setTenantSite(site);
        deploymentRecord.setSitePublication(publication);
        deploymentRecord.setOrchestrationJob(job);
        deploymentRecord.setTriggeredByUserId(actorUserId);
        deploymentRecord.setActionType(actionType);
        deploymentRecord.setDeploymentStatus(SiteDeploymentStatus.IN_PROGRESS);
        deploymentRecord.setRuntimeSpecSnapshot(new LinkedHashMap<>(runtimeSpecSnapshot));
        deploymentRecord.setStartedAt(Instant.now());
        return siteDeploymentRecordRepository.save(deploymentRecord);
    }

    @Transactional
    public void markTenantSiteDeploymentSucceeded(SiteDeploymentRecord deploymentRecord, TenantSiteDeploymentResult deploymentResult) {
        deploymentRecord.setDeploymentStatus(SiteDeploymentStatus.SUCCEEDED);
        deploymentRecord.setResultPayload(new LinkedHashMap<>(deploymentResult.details()));
        deploymentRecord.setCompletedAt(deploymentResult.completedAt());
        siteDeploymentRecordRepository.save(deploymentRecord);
    }

    @Transactional
    public void markTenantSiteDeploymentFailed(SiteDeploymentRecord deploymentRecord, String errorMessage, Map<String, Object> resultPayload) {
        deploymentRecord.setDeploymentStatus(SiteDeploymentStatus.FAILED);
        deploymentRecord.setErrorMessage(errorMessage);
        deploymentRecord.setResultPayload(new LinkedHashMap<>(resultPayload));
        deploymentRecord.setCompletedAt(Instant.now());
        siteDeploymentRecordRepository.save(deploymentRecord);
    }

    @Transactional
    public TenantSite transitionSite(
            TenantSite site,
            TenantSiteLifecycleState nextState,
            String reason,
            String source,
            Map<String, Object> details
    ) {
        TenantSiteLifecycleState previousState = site.getLifecycleState();
        lifecycleTransitions.verifyTransition(previousState, nextState);
        if (previousState == nextState) {
            return site;
        }
        site.setLifecycleState(nextState);
        site.setLastStatusAt(Instant.now());
        tenantSiteRepository.save(site);

        SiteStatusHistory history = new SiteStatusHistory();
        history.setTenantSite(site);
        history.setPreviousState(previousState);
        history.setNewState(nextState);
        history.setTransitionReason(reason);
        history.setTransitionSource(source);
        history.setDetails(new LinkedHashMap<>(details));
        siteStatusHistoryRepository.save(history);
        return site;
    }

    @Transactional
    public void recordAdminAction(
            UUID adminUserId,
            TenantSite site,
            AdminActionType actionType,
            Map<String, Object> beforeStateSummary,
            Map<String, Object> afterStateSummary,
            Map<String, Object> actionDetails
    ) {
        AdminActionHistory history = new AdminActionHistory();
        history.setAdminUserId(adminUserId);
        history.setTenantAccountId(site.getTenantAccount().getId());
        history.setTenantSiteId(site.getId());
        history.setActionType(actionType);
        history.setBeforeStateSummary(new LinkedHashMap<>(beforeStateSummary));
        history.setAfterStateSummary(new LinkedHashMap<>(afterStateSummary));
        history.setActionDetails(new LinkedHashMap<>(actionDetails));
        adminActionHistoryRepository.save(history);
    }

    @Transactional
    public void audit(UUID actorUserId, String subjectType, String subjectId, String eventType, String eventSummary, Map<String, Object> payload) {
        AuditLogEntry auditLogEntry = new AuditLogEntry();
        auditLogEntry.setActorUserId(actorUserId);
        auditLogEntry.setSubjectType(subjectType);
        auditLogEntry.setSubjectId(subjectId);
        auditLogEntry.setEventType(eventType);
        auditLogEntry.setEventSummary(eventSummary);
        auditLogEntry.setPayload(new LinkedHashMap<>(payload));
        auditLogEntryRepository.save(auditLogEntry);
    }
}
