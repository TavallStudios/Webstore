package org.tavall.platform.control.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.tavall.platform.control.config.ControlPlaneProperties;
import org.tavall.platform.persistence.entity.SitePublication;
import org.tavall.platform.persistence.entity.SiteRuntimeDefinition;
import org.tavall.platform.persistence.entity.TenantSite;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;
import org.tavall.platform.runtime.TenantSiteRuntimeResources;
import org.tavall.platform.runtime.TenantSiteRuntimeSpec;

@Component
public class TenantSiteRuntimeSpecFactory {

    private final ControlPlaneProperties controlPlaneProperties;

    public TenantSiteRuntimeSpecFactory(ControlPlaneProperties controlPlaneProperties) {
        this.controlPlaneProperties = controlPlaneProperties;
    }

    public TenantSiteRuntimeSpec buildTenantSiteRuntimeSpec(
            TenantSite site,
            SiteRuntimeDefinition runtimeDefinition,
            SitePublication publication,
            String primaryDomain
    ) {
        Map<String, Object> desiredConfig = runtimeDefinition.getDesiredConfig();
        String databaseSchema = String.valueOf(desiredConfig.getOrDefault("databaseSchema", defaultDatabaseSchema(site.getId(), site.getSlug())));
        TenantRuntimeInfrastructureProfile infrastructureProfile = resolveInfrastructureProfile(desiredConfig.get("infrastructureProfile"));
        Map<String, String> nodeSelector = resolveNodeSelector(desiredConfig.get("nodeSelector"), infrastructureProfile);
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("app.kubernetes.io/name", "webstore-view");
        labels.put("app.kubernetes.io/instance", runtimeDefinition.getVirtualMachineName());
        labels.put("platform.tavall/site-id", site.getId().toString());
        labels.put("platform.tavall/tenant-id", site.getTenantAccount().getId().toString());

        Map<String, String> annotations = new LinkedHashMap<>();
        annotations.put("platform.tavall/publication", publication.getVersionLabel());
        annotations.put("platform.tavall/domain", primaryDomain);
        annotations.put("platform.tavall/infrastructure-profile", infrastructureProfile.name());
        if (!nodeSelector.isEmpty()) {
            annotations.put("platform.tavall/node-selector", formatNodeSelector(nodeSelector));
        }

        Map<String, String> environment = new LinkedHashMap<>();
        environment.put("DATABASE_URL", controlPlaneProperties.getTenantRuntime().getDatabaseUrl());
        environment.put("DATABASE_USERNAME", controlPlaneProperties.getTenantRuntime().getDatabaseUsername());
        environment.put("DATABASE_PASSWORD", controlPlaneProperties.getTenantRuntime().getDatabasePassword());
        environment.put("STOREFRONT_DATABASE_SCHEMA", databaseSchema);
        environment.put("MEDIA_STORAGE_PATH", controlPlaneProperties.getTenantRuntime().getMediaStoragePath());
        environment.put("THEME_STORAGE_PATH", controlPlaneProperties.getTenantRuntime().getThemeStoragePath());
        environment.put("STOREFRONT_RUNTIME_TENANT_ACCOUNT_ID", site.getTenantAccount().getId().toString());
        environment.put("STOREFRONT_RUNTIME_SITE_ID", site.getId().toString());
        environment.put("STOREFRONT_RUNTIME_SITE_SLUG", site.getSlug());
        environment.put("STOREFRONT_RUNTIME_PUBLICATION_VERSION", publication.getVersionLabel());
        environment.put("STOREFRONT_RUNTIME_PRIMARY_DOMAIN", primaryDomain);
        environment.put("STOREFRONT_RUNTIME_INFRASTRUCTURE_PROFILE", infrastructureProfile.name());
        if (!nodeSelector.isEmpty()) {
            environment.put("STOREFRONT_RUNTIME_NODE_SELECTOR", formatNodeSelector(nodeSelector));
        }
        environment.put("SERVER_PORT", "8080");

        return new TenantSiteRuntimeSpec(
                site.getTenantAccount().getId(),
                site.getId(),
                site.getSlug(),
                runtimeDefinition.getRuntimeNamespace(),
                runtimeDefinition.getVirtualMachineName(),
                runtimeDefinition.getServiceName(),
                runtimeDefinition.getIngressName(),
                String.valueOf(desiredConfig.getOrDefault("runtimeBaseImage", controlPlaneProperties.getKubernetes().getRuntimeBaseImage())),
                String.valueOf(desiredConfig.getOrDefault("bootstrapArtifactUrl", controlPlaneProperties.getTenantRuntime().getBootstrapArtifactUrl())),
                String.valueOf(desiredConfig.getOrDefault("bootstrapArtifactSha256", controlPlaneProperties.getTenantRuntime().getBootstrapArtifactSha256())),
                publication.getVersionLabel(),
                databaseSchema,
                primaryDomain,
                infrastructureProfile,
                new TenantSiteRuntimeResources(
                        runtimeDefinition.getDesiredCpuCores(),
                        runtimeDefinition.getDesiredMemoryMiB(),
                        runtimeDefinition.getDesiredStorageGiB()
                ),
                nodeSelector,
                labels,
                annotations,
                environment
        );
    }

    private TenantRuntimeInfrastructureProfile resolveInfrastructureProfile(Object configuredValue) {
        if (configuredValue == null) {
            return controlPlaneProperties.getKubernetes().getInfrastructureProfile();
        }
        try {
            return TenantRuntimeInfrastructureProfile.valueOf(String.valueOf(configuredValue).trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return controlPlaneProperties.getKubernetes().getInfrastructureProfile();
        }
    }

    private Map<String, String> resolveNodeSelector(Object configuredValue, TenantRuntimeInfrastructureProfile infrastructureProfile) {
        if (configuredValue instanceof Map<?, ?> configuredMap) {
            Map<String, String> selector = new LinkedHashMap<>();
            configuredMap.forEach((key, value) -> {
                if (key != null && value != null) {
                    selector.put(String.valueOf(key), String.valueOf(value));
                }
            });
            return selector;
        }
        if (configuredValue != null && StringUtils.hasText(String.valueOf(configuredValue))) {
            return parseNodeSelector(String.valueOf(configuredValue));
        }
        return switch (infrastructureProfile) {
            case DEDICATED_KUBEVIRT -> parseNodeSelector(controlPlaneProperties.getKubernetes().getDedicatedNodeSelector());
            case NESTED_KUBEVIRT -> parseNodeSelector(controlPlaneProperties.getKubernetes().getNestedNodeSelector());
            case AUTO -> Map.of();
        };
    }

    private Map<String, String> parseNodeSelector(String configuredSelector) {
        if (!StringUtils.hasText(configuredSelector)) {
            return Map.of();
        }
        Map<String, String> selector = new LinkedHashMap<>();
        for (String token : configuredSelector.split(",")) {
            String trimmedToken = token.trim();
            if (!StringUtils.hasText(trimmedToken)) {
                continue;
            }
            String[] parts = trimmedToken.split("=", 2);
            if (parts.length == 2 && StringUtils.hasText(parts[0])) {
                selector.put(parts[0].trim(), parts[1].trim());
            }
        }
        return selector;
    }

    private String formatNodeSelector(Map<String, String> nodeSelector) {
        return nodeSelector.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    public String defaultDatabaseSchema(UUID siteId, String siteSlug) {
        return "store_" + siteSlug.replace('-', '_') + "_" + siteId.toString().replace("-", "").substring(0, 8);
    }
}
