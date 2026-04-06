package org.tavall.platform.core.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;
import org.tavall.platform.runtime.TenantSiteRuntimeResources;

public record CreateTenantSiteCommand(
        UUID tenantAccountId,
        UUID requestedByUserId,
        @NotBlank String siteName,
        @NotBlank String siteSlug,
        String requestedDomain,
        @Valid TenantSiteRuntimeResources resources,
        String publicationVersion,
        Map<String, Object> siteSettingsDraft
) {
}
