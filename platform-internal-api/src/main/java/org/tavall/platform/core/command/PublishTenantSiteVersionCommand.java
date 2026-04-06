package org.tavall.platform.core.command;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

public record PublishTenantSiteVersionCommand(
        UUID siteId,
        UUID requestedByUserId,
        @NotBlank String versionLabel,
        @NotBlank String buildReference,
        Map<String, Object> runtimeConfigSnapshot,
        Map<String, Object> storeConfigSnapshot,
        Map<String, Object> metadata
) {
}
