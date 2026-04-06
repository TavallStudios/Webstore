package org.tavall.platform.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record KubeVirtClusterCompatibilityReport(
        TenantRuntimeInfrastructureProfile infrastructureProfile,
        boolean compatible,
        int schedulableNodeCount,
        int arm64NodeCount,
        int x86NodeCount,
        int kvmCapableNodeCount,
        boolean emulationEnabled,
        Map<String, String> nodeSelector,
        String message
) {

    public Map<String, Object> asDetails() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("infrastructureProfile", infrastructureProfile.name());
        details.put("compatible", compatible);
        details.put("schedulableNodeCount", schedulableNodeCount);
        details.put("arm64NodeCount", arm64NodeCount);
        details.put("x86NodeCount", x86NodeCount);
        details.put("kvmCapableNodeCount", kvmCapableNodeCount);
        details.put("emulationEnabled", emulationEnabled);
        if (nodeSelector != null && !nodeSelector.isEmpty()) {
            details.put("nodeSelector", new LinkedHashMap<>(nodeSelector));
        }
        details.put("message", message);
        return details;
    }
}
