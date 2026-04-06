package org.tavall.platform.runtime;

import java.util.Map;
import java.util.UUID;

public record TenantSiteDeploymentRequest(
        UUID siteId,
        UUID publicationId,
        UUID triggeredByUserId,
        String action,
        TenantSiteRuntimeSpec runtimeSpec,
        Map<String, Object> metadata
) {
}
