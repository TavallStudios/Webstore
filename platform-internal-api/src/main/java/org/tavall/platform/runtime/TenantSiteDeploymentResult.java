package org.tavall.platform.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record TenantSiteDeploymentResult(
        boolean successful,
        String deploymentReference,
        String message,
        TenantSiteRuntimeStatus runtimeStatus,
        Map<String, Object> details,
        Instant completedAt
) {
}
