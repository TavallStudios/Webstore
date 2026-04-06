package org.tavall.platform.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record TenantSiteRuntimeSpec(
        UUID tenantId,
        UUID siteId,
        String siteSlug,
        String namespace,
        String virtualMachineName,
        String serviceName,
        String ingressName,
        String baseImage,
        String bootstrapArtifactUrl,
        String bootstrapArtifactSha256,
        String publicationVersion,
        String runtimeDatabaseSchema,
        String primaryDomain,
        TenantRuntimeInfrastructureProfile infrastructureProfile,
        TenantSiteRuntimeResources resources,
        Map<String, String> nodeSelector,
        Map<String, String> labels,
        Map<String, String> annotations,
        Map<String, String> environment
) {
}
