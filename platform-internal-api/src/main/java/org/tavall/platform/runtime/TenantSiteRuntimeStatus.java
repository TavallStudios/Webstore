package org.tavall.platform.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record TenantSiteRuntimeStatus(
        TenantSiteRuntimePowerState powerState,
        String runtimePhase,
        String namespace,
        String virtualMachineName,
        String serviceName,
        String ingressHost,
        String message,
        Instant observedAt,
        Map<String, Object> details
) {
}
