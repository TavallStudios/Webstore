package org.tavall.platform.admin.view;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlatformAdminViewModels {

    private PlatformAdminViewModels() {
    }

    public record AdminDashboardSummary(
            long totalTenants,
            long totalSites,
            long runningSites,
            long stoppedSites,
            long failedSites,
            List<AdminDeploymentRow> recentDeployments,
            List<AdminJobRow> recentJobs
    ) {
    }

    public record AdminTenantRow(
            UUID userId,
            String displayName,
            String email,
            String roles,
            UUID tenantAccountId,
            String tenantDisplayName,
            String onboardingState,
            long siteCount,
            Instant createdAt
    ) {
    }

    public record AdminSiteRow(
            UUID siteId,
            String siteName,
            String siteSlug,
            String tenantDisplayName,
            String lifecycleState,
            String readinessState,
            String runtimeResources,
            String primaryDomain,
            String publicationVersion,
            Instant updatedAt
    ) {
    }

    public record AdminSiteDetailView(
            AdminSiteRow site,
            String runtimeNamespace,
            String virtualMachineName,
            String serviceName,
            String ingressName,
            String runtimePhase,
            String runtimeMessage,
            List<AdminDomainRow> domains,
            List<AdminPublicationRow> publications,
            List<AdminDeploymentRow> deployments,
            List<AdminStatusRow> statusHistory,
            List<AdminMutationRow> mutations
    ) {
    }

    public record AdminDomainRow(String host, String status, boolean primaryDomain) {
    }

    public record AdminPublicationRow(String versionLabel, String status, String buildReference, Instant publishedAt) {
    }

    public record AdminDeploymentRow(String siteName, String actionType, String status, String message, String versionLabel, Instant createdAt) {
    }

    public record AdminJobRow(String siteName, String jobType, String jobStatus, int retryCount, String lastError, Instant queuedAt) {
    }

    public record AdminStatusRow(String previousState, String newState, String reason, Instant createdAt) {
    }

    public record AdminMutationRow(Instant createdAt, String status, Map<String, Object> requestedResources) {
    }

    public record AdminAuditRow(String actionType, String subject, String summary, Instant createdAt) {
    }
}
